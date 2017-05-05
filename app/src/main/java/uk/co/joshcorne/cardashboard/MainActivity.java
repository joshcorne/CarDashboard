package uk.co.joshcorne.cardashboard;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
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

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import uk.co.joshcorne.cardashboard.models.Journey;
import uk.co.joshcorne.cardashboard.models.Ping;

public class MainActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback
{

    public static boolean TRACKING = false;
    public Intent tracker;
    public boolean PLAYING_MUSIC = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }

    @Override
    protected void onDestroy()
    {
        stopService(tracker);
        unregisterReceiver(updateUi);
        unregisterReceiver(updateAlerts);
        if(PLAYING_MUSIC)
        {
            Spotify.destroyPlayer(this);
            PLAYING_MUSIC = false;
        }
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
        playMusic(view);
        Snackbar.make(view, "Not implemented.", Snackbar.LENGTH_SHORT).show();
    }

    public void stopMusic(View view)
    {
        if(PLAYING_MUSIC)
        {
            mPlayer.pause(null);
        }
    }

    public void skipSong(View view)
    {
        if(PLAYING_MUSIC)
        {
            mPlayer.skipToPrevious(null);
        }
    }

    public void previousSong(View view)
    {
        if(PLAYING_MUSIC)
        {
            mPlayer.skipToNext(null);
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
        startActivity(new Intent(this, AlertsActivity.class));
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
                PLAYING_MUSIC = true;
            }
            else
            {
                playButton.setText(getResources().getString(R.string.pause_button_text));
                playButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        startMusic(view);
                    }
                });
                PLAYING_MUSIC = false;
            }
        }
    }

    public void calculateNewStats()
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sp.edit();
        List<Ping> pings = new ArrayList<>();
        float avgSpeed = 0;
        for (Ping p : pings)
        {
            avgSpeed += p.getSpeed();
        }
        edit.putFloat("avgSpeed", avgSpeed);
        edit.apply();
    }

    protected double getAvgSpeed(List<Journey> journeys)
    {
        double avgSpeed = 0;
        for(Journey j: journeys)
        {
            avgSpeed += j.getAvgSpeed();
        }
        return avgSpeed / journeys.size();
    }

    private static final String CLIENT_ID = "c7ec83d0851649a4845bf6354fdd7c9f";
    private static final String REDIRECT_URI = "cardash://callback";
    private static final int REQUEST_CODE = 1337;
    private Player mPlayer;
    private static final String SPOTIFYAPI = "https://api.spotify.com/v1/me/tracks";
    private static final String SPOTIFYMARKET = "GB";
    private static String OAUTH;

    public void playMusic(View view)
    {
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

            if (pressure != null && revs != null && speed != null && consumption != null)
            {
                pressure.setText(getString(R.string.live_fuel_pressure) + " " + intent.getDoubleExtra("pressureVal", 0.0));
                revs.setText(getString(R.string.live_revs) + " " + intent.getDoubleExtra("revsVal", 0.0));
                speed.setText(getString(R.string.live_speed) + " " + intent.getDoubleExtra("speedVal", 0.0));
                consumption.setText(getString(R.string.live_fuel_consumption) + " " + intent.getDoubleExtra("consumptionVal", 0.0));
            }
        }
    };

    private BroadcastReceiver updateAlerts = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            //TODO: CHANGE
            ListView listView = (ListView) findViewById(R.id.journeyList);
            //TODO: Populate list
            List<String> descriptions = intent.getStringArrayListExtra("descriptions");
        }
    };

    @Override
    public void onLoggedIn()
    {
        List<String> trackUris = new ArrayList<>();

        String json = null;

        //This should 100% not be done on the UI thread.
        try
        {
            URL url = new URL(SPOTIFYAPI + "?market=" + SPOTIFYMARKET);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.addRequestProperty("Authorization", OAUTH);
            urlConnection.addRequestProperty("Content-type", "application/json");
            urlConnection.addRequestProperty("Accept", "application/json");

            try
            {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                json = stringBuilder.toString();
            }
            finally
            {
                urlConnection.disconnect();
            }
        }
        catch (Exception e)
        {
            Log.e("ERROR", e.getMessage(), e);
            Toast.makeText(this, "Loading music failed.", Toast.LENGTH_SHORT).show();
        }

        //Pull URIs out of JSON
        try
        {
            JSONObject obj = new JSONObject(json);
            JSONArray tracks = obj.getJSONArray("items");

            if (tracks != null)
            {
                for (int i = 0; i < tracks.length(); i++)
                {
                    //Traverse the JSON to the URI and add to list
                    trackUris.add(tracks.getJSONObject(i).getJSONObject("track").getString("uri"));
                }
            }

        }
        catch (JSONException e)
        {
            Toast.makeText(this, "Loading music failed.", Toast.LENGTH_SHORT).show();
        }

        if (trackUris.size() > 0)
        {
            //Play first track
            mPlayer.playUri(null, trackUris.get(0), 0, 0);

            PLAYING_MUSIC = true;

            if (trackUris.size() > 1)
            {
                //Queue the rest of their tracks
                for (int i = 1; i < trackUris.size(); i++)
                {
                    mPlayer.queue(null, trackUris.get(i));
                }
            }
        }

    }

    @Override
    public void onLoggedOut()
    {

    }

    @Override
    public void onLoginFailed(Error error)
    {

    }

    @Override
    public void onTemporaryError()
    {

    }

    @Override
    public void onConnectionMessage(String s)
    {

    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent)
    {
        switch (playerEvent)
        {
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

                    }
                });
            }
        }
    }
}
