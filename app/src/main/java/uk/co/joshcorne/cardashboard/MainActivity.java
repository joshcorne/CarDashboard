package uk.co.joshcorne.cardashboard;

import android.Manifest;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.orm.SugarContext;
import com.orm.query.Select;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyNativeAuthUtil;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.methods.GetMySavedTracksRequest;
import com.wrapper.spotify.models.LibraryTrack;
import com.wrapper.spotify.models.Page;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.Permission;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import uk.co.joshcorne.cardashboard.models.Journey;
import uk.co.joshcorne.cardashboard.models.Ping;
import uk.co.joshcorne.cardashboard.models.TroubleCode;

import static uk.co.joshcorne.cardashboard.SettingsActivity.ObdPreferenceFragment.sock;

public class MainActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback
{

    public static boolean TRACKING = false;
    public static Intent tracker;
    private ArrayList<String> troubleCodes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** TO DELETE **
        Journey.deleteAll(Journey.class);
        Ping.deleteAll(Ping.class);

        Date d;
        final Random random = new Random();
        final int millisInDay = 24*60*60*1000;
        try
        {
            for(int i = 1; i <18; i++)
            {
                Time time = new Time((long)random.nextInt(millisInDay));
                d = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2017-04-" + i + " " + time.toString());
                Journey j = new Journey(d);
                j.setDistance(4 + ( 200 - 4) * random.nextDouble());
                j.save();
                for(int a = 0; a<10; a++)
                {
                    Ping p = new Ping(j);
                    p.setSpeed(70 * random.nextDouble());
                    p.setFuelPressure(45 + (78-45) * random.nextDouble());
                    p.setMpg(35 + (50 - 35) * random.nextDouble());
                    p.setRpm((random.nextInt() % (4000 - 1500 + 1)) + 1);
                    p.save();
                }
            }
        }
        catch (ParseException p)
        {
            Log.d("DARCASH", "parse excpetion");
        }
        /***************

        try
        {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if(sd.canWrite())
            {
                String cur = "/data/data/" + getPackageName() + "/databases/car.db";
                String bak = "bak.db";

                File curDb = new File(cur);
                File bakDb = new File(sd, bak);

                if(curDb.exists())
                {
                    FileChannel src = new FileInputStream(curDb).getChannel();
                    FileChannel out = new FileOutputStream(bakDb).getChannel();
                    out.transferFrom(src, 0, src.size());
                    src.close();
                    out.close();
                }
            }
        }
        catch (Exception e)
        {
            Log.e("a", e.getMessage());
        }

        this.finishAffinity();*/

        if(!checkDataBase())
        {
            try
            {
                copyDataBase();
                Log.d("CARDASH", "DB copied.");
            }
            catch (IOException e)
            {
                Toast.makeText(this, "Setup failed.", Toast.LENGTH_SHORT).show();
                Log.e("CARDASH", "Could not copy DB.");
            }
        }

        tracker = new Intent(this, TrackerService.class);

        registerReceiver(updateUi, new IntentFilter("VALUES_UPDATED"));
        registerReceiver(updateAlerts, new IntentFilter("ALERTS_UPDATED"));

        if (TRACKING)
        {
            toggleJourneyBtnState();
        }
        ((ScrollView) findViewById(R.id.alerts_panel))
                .setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        openAlerts(v);
                    }
                });
        ((ScrollView) findViewById(R.id.stats_panel))
                .setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        openStats(v);
                    }
                });

        TextView allTimeConsumptionAvg = (TextView) findViewById(R.id.allTimeConsumptionAvgTextView);
        TextView allTimeSpeedAvg = (TextView) findViewById(R.id.allTimeSpeedAvgTextView);
        TextView allTimeRevsAvg = (TextView) findViewById(R.id.allTimeRevsAvgTextView);
        TextView allTimePressureAvg = (TextView) findViewById(R.id.allTimePressureAvgTextView);

        Averages avg = getAvgConsumption(Select.from(Journey.class).list());
        if(avg != null && avg.speed != 0)
        {
            allTimeSpeedAvg.setText("Speed avg: " + String.format("%.2f", avg.speed) + "MPH");
        }
        if(avg != null && avg.consumption != 0)
        {
            allTimeConsumptionAvg.setText("Consumption avg (litres/hour): " + String.format("%.2f", avg.consumption));
        }
        if(avg != null && avg.revs != 0)
        {
            allTimeRevsAvg.setText("Revs avg: " + avg.revs + "RPM");
        }
        if(avg != null && avg.pressure != 0)
        {
            allTimePressureAvg.setText("Pressure avg: " + String.format("%.2f", avg.pressure) + "kPa");
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopService(tracker);
        unregisterReceiver(updateUi);
        unregisterReceiver(updateAlerts);
        if(mPlayer != null)
        {
            Spotify.destroyPlayer(this);
        }
        if(sock != null)
        {
            try
            {
                sock.close();
            }
            catch(Exception e)
            {
                Log.d("CARDASH", "Socket not closed properly.");
            }
        }
    }

    protected void copyDataBase() throws IOException
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            //Open your local db as the input stream
            InputStream myInput = this.getAssets().open("car.db");

            // Path to the just created empty db
            String outFileDir = this.getFilesDir().getPath() + File.separator + "databases";
            String outFileName = outFileDir + File.separator + "car.db";

            File file = new File(outFileDir);
            file.mkdirs();

            //Open the empty db as the output stream
            OutputStream myOutput = new FileOutputStream(outFileName);

            //transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0)
            {
                myOutput.write(buffer, 0, length);
            }

            //Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();

            InputStream input = this.getAssets().open("TROUBLE_CODE.csv");
            String out = this.getFilesDir().getPath() + File.separator + "trouble.csv";

            OutputStream outputStream = new FileOutputStream(out);

            byte[] buf = new byte[1024];
            int len;
            while((len = input.read(buf)) > 0)
            {
                outputStream.write(buf, 0, len);
            }

            input.close();
            outputStream.flush();
            outputStream.close();

            String csv = out;
            BufferedReader br = null;
            String line;
            String csvSplitBy = ",";

            try
            {
                br = new BufferedReader(new FileReader(csv));
                while((line = br.readLine()) != null)
                {
                    String[] values = line.split(csvSplitBy);

                    TroubleCode t = new TroubleCode(values[0], values[1]);
                    t.setDtcValue(values[2]);
                    t.save();
                }
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Trouble codes copying failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    protected boolean checkDataBase()
    {
        SQLiteDatabase checkDB = null;
        try
        {
            String myPath = getApplicationContext().getFilesDir().getPath() + "/databases/" + "car.db";
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }
        catch(SQLiteException e)
        {
            Log.d("CARDASH", "DB does not exist yet.");
            //database does't exist yet.

        }

        if(checkDB != null)
        {
            checkDB.close();
        }

        return checkDB != null;
    }

    private void openGoogleMaps(String search)
    {
        Uri uri = Uri.parse("google.navigation:q=" + search);
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setPackage("com.google.android.apps.maps");
        startActivity(i);
    }

    public void goSomewhere(View view)
    {
        //Get address from search box
        EditText mapsSearch = (EditText) findViewById(R.id.mapsSearch);
        if (mapsSearch != null)
        {
            openGoogleMaps(mapsSearch.getText().toString());
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("general_track_on_nav", true))
            {
                //Start recording if setting allows
                startJourney(view);
            }
        }
        else
        {
            Snackbar.make(view, "Input a location", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void goHome(View view)
    {
        openGoogleMaps(PreferenceManager.getDefaultSharedPreferences(this).getString("general_home_addr", ""));
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("general_track_on_nav", true))
        {
            //Start recording if setting allows
            startJourney(view);
        }
    }

    public void openSettings(View view)
    {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    public void startMusic(View view)
    {
        if(mPlayer != null)
        {
            Log.d("CARDASH", "Music resumed.");
            mPlayer.resume(null);
            toggleMusicBtnState();
        }
        else
        {
            playMusic(view);
        }
    }

    public void stopMusic(View view)
    {
        if(mPlayer != null && mPlayer.getPlaybackState().isPlaying)
        {
            Log.d("CARDASH", "Music paused.");
            mPlayer.pause(null);
            toggleMusicBtnState();
        }
    }

    public void skipSong(View view)
    {
        if(mPlayer != null && mPlayer.getPlaybackState().isPlaying && mPlayer.getMetadata().nextTrack != null)
        {
            Log.d("CARDASH", "Skipping song.");
            mPlayer.skipToNext(null);
        }
    }

    public void previousSong(View view)
    {
        if(mPlayer != null && mPlayer.getPlaybackState().isPlaying && mPlayer.getMetadata().prevTrack != null)
        {
            Log.d("CARDASH", "Previous song.");
            mPlayer.skipToPrevious(null);
        }
    }

    public void startJourney(View view)
    {
        if (SettingsActivity.OBDCONNECTED)
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                Snackbar.make(view, "Location services not allowed.", Snackbar.LENGTH_SHORT).show();
            }
            else
            {
                //Start service listening for GPS etc. pings
                Log.i("CarDash", "Journey Started");
                Snackbar.make(view, "Journey Started", Snackbar.LENGTH_SHORT).show();
                startService(new Intent(this, TrackerService.class));
                toggleJourneyBtnState();
            }
        }
        else
        {
            Snackbar.make(view, "Connect to your reader first.", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void endJourney(View view)
    {
        Log.i("CarDash", "Journey Ended");
        stopService(tracker);
        toggleJourneyBtnState();
    }

    public void openStats(View view)
    {
        startActivity(new Intent(this, StatsActivity.class));
    }

    public void openAlerts(View view)
    {
        Intent i = new Intent(this, AlertsActivity.class);
        i.putStringArrayListExtra("codes", troubleCodes);
        startActivity(i);
    }

    private void toggleJourneyBtnState()
    {
        Button journeyButton = (Button) findViewById(R.id.journeyBtn);
        if (journeyButton != null)
        {
            if (journeyButton.getText().toString().equals(getResources().getString(R.string.start_btn_text)))
            {
                journeyButton.setText(getResources().getString(R.string.end_btn_text));
                journeyButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        endJourney(view);
                    }
                });
                TRACKING = true;
            }
            else
            {
                journeyButton.setText(getResources().getString(R.string.start_btn_text));
                journeyButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        startJourney(view);
                    }
                });
                TRACKING = false;
            }
        }
    }

    private void toggleMusicBtnState()
    {
        Button playButton = (Button) findViewById(R.id.playMusicBtn);
        if (playButton != null)
        {
            if (playButton.getText().toString().equals(getResources().getString(R.string.play_button_text)))
            {
                playButton.setText(getResources().getString(R.string.pause_button_text));
                playButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        stopMusic(view);
                    }
                });
            }
            else
            {
                playButton.setText(getResources().getString(R.string.play_button_text));
                playButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        startMusic(view);
                    }
                });
            }
        }
    }

    protected double getAvgSpeed(List<Journey> journeys)
    {
        if(journeys.size() > 0)
        {
            double avgSpeed = 0;
            for (Journey j : journeys)
            {
                avgSpeed += j.getAvgSpeed();
            }
            return avgSpeed / journeys.size();
        }
        else
        {
            return 0;
        }
    }

    class Averages{
        Averages(double speed, int revs, double pressure, double consumption)
        {
            this.speed = speed;
            this.revs = revs;
            this.pressure = pressure;
            this.consumption = consumption;
        }

        double speed;
        int revs;
        double consumption;
        double pressure;
    }

    protected Averages getAvgConsumption(List<Journey> journeys)
    {
        if(journeys.size() > 0)
        {
            double avgSpeed = 0;
            int avgRevs = 0;
            double avgPressure = 0;
            double avgConsumption = 0;
            for (Journey j : journeys)
            {
                avgSpeed += j.getAvgSpeed();
                avgRevs += j.getAvgRevs();
                avgPressure += j.getAvgPressure();
                avgConsumption += j.getAvgConsumption();
            }
            int size = journeys.size();
            return new Averages(avgSpeed / size, avgRevs / size, avgPressure / size, avgConsumption / size);
        }
        else
        {
            return null;
        }
    }

    private final String CLIENT_ID = "c7ec83d0851649a4845bf6354fdd7c9f";
    private final String REDIRECT_URI = "cardash://callback";
    private final int REQUEST_CODE = 1337;
    protected static Player mPlayer;
    protected static String OAUTH;
    private ProgressDialog progressDialog;
    private int queueCounter = 0;
    Page<LibraryTrack> tracks = null;
    private Player.OperationCallback callback = new Player.OperationCallback()
    {
        @Override
        public void onSuccess()
        {
            if(queueCounter<tracks.getItems().size())
            {
                mPlayer.queue(callback, tracks.getItems().get(++queueCounter).getTrack().getUri());
            }
        }

        @Override
        public void onError(Error error)
        {
            Log.d("CARDASH", "Queueing failed.");
        }
    };

    public void playMusic(View view)
    {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Wait while loading...");
        progressDialog.show();

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming", "user-library-read"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    private BroadcastReceiver updateUi = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            TextView pressure = (TextView) findViewById(R.id.pressureTextView);
            TextView revs = (TextView) findViewById(R.id.revsTextView);
            TextView speed = (TextView) findViewById(R.id.speedTextView);
            TextView consumption = (TextView) findViewById(R.id.consumptionTextView);

            double pressureVal = intent.getDoubleExtra("pressureVal", 0.0);
            double revsVal = intent.getIntExtra("revsVal", 0);
            double speedVal = intent.getDoubleExtra("speedVal", 0.0);
            double consumptionVal = intent.getDoubleExtra("consumptionVal", 0.0);

            if (pressure != null && revs != null && speed != null && consumption != null)
            {
                pressure.setText(getString(R.string.live_fuel_pressure) + " " + String.format("%.2f", pressureVal) + "kPa");
                revs.setText(getString(R.string.live_revs) + " " + revsVal + "RPM");
                speed.setText(getString(R.string.live_speed) + " " + String.format("%.2f", speedVal) + "MPH");
                consumption.setText(getString(R.string.live_fuel_consumption) + " " + String.format("%.2f", consumptionVal) + " litres/hour");
            }
        }
    };

    private BroadcastReceiver updateAlerts = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            TextView code = (TextView) findViewById(R.id.code);
            ArrayList<String> response = intent.getStringArrayListExtra("code");
            if(response != null && response.size() > 0)
            {
                String s = "";
                for (int i = 0; i < response.size(); i++)
                {
                    troubleCodes.add(response.get(i));
                    s = s + response.get(i);
                    if(i < (response.size() - 1))
                    {
                        s = s + ", ";
                    }
                    code.setText(s);
                }
            }
        }
    };

    @Override
    public void onLoggedIn()
    {
        Api api = Api.builder().accessToken(OAUTH).build();

        GetMySavedTracksRequest request = api.getMySavedTracks()
                .limit(50)
                .build();

        MusicTask ct = new MusicTask();

        try
        {
            tracks = ct.execute(request).get();
        }
        catch (Exception e)
        {
            Log.e("CARDASH", e.getMessage());
        }

        if(tracks != null)
        {
            //Play first track
            final Page<LibraryTrack> finalTracks = tracks;
            mPlayer.playUri(new Player.OperationCallback()
            {
                @Override
                public void onSuccess()
                {
                    mPlayer.queue(callback, tracks.getItems().get(++queueCounter).getTrack().getUri());

                    toggleMusicBtnState();

                    progressDialog.dismiss();

                    //Shuffle so it isn't the same all the time
                    mPlayer.setShuffle(null, true);
                    //Repeat to keep the tunes going
                    mPlayer.setRepeat(null, true);

                }

                @Override
                public void onError(Error error)
                {
                    Toast.makeText(getApplicationContext(), "Playback failure.", Toast.LENGTH_SHORT).show();
                }
            }, "spotify:user:spotify:playlist:37i9dQZEVXcFiuHqbpjDn7", 1, 0);
        }
        else
        {
            Toast.makeText(this, "Could not get user tracks.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoggedOut()
    {
        Log.d("CARDASH", "onLoggedOut");
    }

    @Override
    public void onLoginFailed(Error error)
    {

        if(error == Error.kSpErrorNeedsPremium)
        {
            Toast.makeText(this, "You need Spotify premium.", Toast.LENGTH_SHORT).show();
        }
        Log.d("CARDASH", error.toString());
    }

    @Override
    public void onTemporaryError()
    {
        Log.d("CARDASH", "onTemporaryError");
    }

    @Override
    public void onConnectionMessage(String s)
    {
        Log.d("CARDASH", "onConnectionMessage");
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent)
    {
        switch (playerEvent)
        {
            case kSpPlaybackNotifyTrackDelivered:
                Log.d("CARDASH", "Track delivered.");
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error)
    {
        switch (error)
        {
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode == REQUEST_CODE)
        {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if(response.getType() == AuthenticationResponse.Type.TOKEN)
            {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                OAUTH = response.getAccessToken();
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver()
                {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer)
                    {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable)
                    {
                        Log.e("CARDASH", "Spotify.getPlayer.onError");
                    }
                });
            }
        }
    }
}

class MusicTask extends AsyncTask<GetMySavedTracksRequest, Void, Page<LibraryTrack>>
{
    protected Page<LibraryTrack> doInBackground(GetMySavedTracksRequest... params) {
        try
        {
            return params[0].get();
        }
        catch(Exception e)
        {
            return null;
        }
    }

    protected void onPostExecute(String... params) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }
}

