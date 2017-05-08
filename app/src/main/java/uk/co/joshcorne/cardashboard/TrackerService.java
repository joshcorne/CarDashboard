package uk.co.joshcorne.cardashboard;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.pressure.FuelPressureCommand;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.joshcorne.cardashboard.models.Journey;
import uk.co.joshcorne.cardashboard.models.Ping;

import static uk.co.joshcorne.cardashboard.SettingsActivity.ObdPreferenceFragment.sock;

public class TrackerService extends Service
{
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLocation;
    private boolean recordLocation = true;

    private static final String TAG = "CARDASH";
    private final IBinder binder = new ServiceBinder();
    NotificationManager notificationManager;
    public static final int NOTIFICATION_ID = 1;

    protected boolean isRunning = false;

    private Timer timer = new Timer();
    private static final long UPDATE_INTERVAL = 5000;

    private Journey journey;

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.d(TAG, "TrackerService.onCreate");

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                mLocation = location;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle)
            {

            }

            @Override
            public void onProviderEnabled(String s)
            {

            }

            @Override
            public void onProviderDisabled(String s)
            {

            }
        };

        //Create a new journey with the current timestamp
        journey = new Journey(new Date());
        startRecordingLocation();
        startRecordingEcu();

        //Create notification
        notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setOngoing(true)
                .setContentTitle("Car Dashboard")
                .setContentText("Recording Journey")
                .setContentIntent(pendingIntent)
                .build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "TrackerService.onDestroy");
        super.onDestroy();
        //Cancel ALL THE THINGS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            //Stop watching the location updates
            mLocationManager.removeUpdates(mLocationListener);
        }
        if (timer != null)
        {
            //Stop recording the data
            timer.cancel();
        }
        if (journey != null)
        {
            //Save the journey in the DB
            journey.save();
        }
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void startRecordingEcu()
    {
        try
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED || !SettingsActivity.OBDCONNECTED)
            {
                // Wi-Fi P2P is not enabled
                throw new Exception("WiFi is not enabled or OBD not connected.");
            }
            else
            {
                // Wifi is enabled
                startOdbConnection();
            }
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Failed to connect to ECU. " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void startOdbConnection() throws IOException, InterruptedException
    {
        //Regular data requests
        final RPMCommand rpmCommand = new RPMCommand();
        final FuelPressureCommand fuelPressureCommand = new FuelPressureCommand();
        final SpeedCommand speedCommand = new SpeedCommand();
        final ConsumptionRateCommand consumptionRateCommand = new ConsumptionRateCommand();

        //Set a timer to grab the data every so often
        timer.scheduleAtFixedRate(new TimerTask()
                                  {
                                      @Override
                                      public void run()
                                      {
                                          Ping p = new Ping();
                                          if(recordLocation)
                                          {
                                              p.setLatitude(mLocation.getLatitude());
                                              p.setLongitude(mLocation.getLongitude());
                                              p.setAltitude(mLocation.getAltitude());
                                          }

                                          try
                                          {
                                              rpmCommand.run(sock.getInputStream(), sock.getOutputStream());
                                              p.setRpm(rpmCommand.getRPM());

                                              fuelPressureCommand.run(sock.getInputStream(), sock.getOutputStream());
                                              p.setFuelPressure(Float.valueOf(fuelPressureCommand.getResult()));

                                              speedCommand.run(sock.getInputStream(), sock.getOutputStream());
                                              p.setSpeed(speedCommand.getImperialSpeed());

                                              consumptionRateCommand.run(sock.getInputStream(), sock.getOutputStream());
                                              p.setMpg(consumptionRateCommand.getLitersPerHour());

                                              //Once we have all the ping data, add it to the journey
                                              journey.addPing(p);

                                              Intent i = new Intent("VALUES_UPDATED");
                                              i.putExtra("revsVal", p.getRpm());
                                              i.putExtra("pressureVal", p.getFuelPressure());
                                              i.putExtra("consumptionVal", p.getMpg());
                                              i.putExtra("speedVal", p.getSpeed());

                                              sendBroadcast(i);
                                          }
                                          catch (InterruptedException e)
                                          {
                                              Toast.makeText(getApplicationContext(),
                                                      "Recording data interrupted. " + e.getMessage(),
                                                      Toast.LENGTH_SHORT).show();
                                          }
                                          catch (IOException e)
                                          {
                                              Toast.makeText(getApplicationContext(),
                                                      "IO error in recording data. " + e.getMessage(),
                                                      Toast.LENGTH_SHORT).show();

                                          }
                                      }
                                  },
                0,
                UPDATE_INTERVAL);
        sock.close();
    }

    private void startRecordingLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    public class ServiceBinder extends Binder
    {
        public TrackerService getService()
        {
            return TrackerService.this;
        }
    }
}