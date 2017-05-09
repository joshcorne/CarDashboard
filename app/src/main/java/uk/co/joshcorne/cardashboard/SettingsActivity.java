package uk.co.joshcorne.cardashboard;


import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.co.joshcorne.cardashboard.SettingsActivity.ObdPreferenceFragment.ip;
import static uk.co.joshcorne.cardashboard.SettingsActivity.ObdPreferenceFragment.oem;
import static uk.co.joshcorne.cardashboard.SettingsActivity.ObdPreferenceFragment.port;
import static uk.co.joshcorne.cardashboard.SettingsActivity.ObdPreferenceFragment.sock;
import static uk.co.joshcorne.cardashboard.SettingsActivity.connectionLoading;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity
{
    private static final String TAG = "CARDASH";
    public static boolean OBDCONNECTED = false;
    protected static ProgressDialog connectionLoading;
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener()
    {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value)
        {
            String stringValue = value.toString();

            if (preference instanceof ListPreference)
            {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            }
            else
            {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context)
    {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference)
    {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setupActionBar();

        port = PreferenceManager.getDefaultSharedPreferences(this).getString("general_ip_port", "35000");
        ip = PreferenceManager.getDefaultSharedPreferences(this).getString("general_ip_addr", "192.168.0.10");
        oem = PreferenceManager.getDefaultSharedPreferences(this).getString("car_make", "unset");
        connectionLoading = new ProgressDialog(this);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar()
    {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            // Show the Up button in the action bar.
            //TODO actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane()
    {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target)
    {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName)
    {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || ObdPreferenceFragment.class.getName().equals(fragmentName);
    }

    public static class ObdPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_obd);
            setHasOptionsMenu(true);

            Preference addrPref = findPreference("general_ip_addr");
            Preference portPref = findPreference("general_ip_port");
            if(addrPref != null)
            {
                addrPref.setSummary(ip);
                addrPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
                {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o)
                    {
                        ip = o.toString();
                        preference.setSummary(o.toString());
                        return true;
                    }
                });
            }
            if(portPref != null)
            {
                portPref.setSummary(port);
                portPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
                {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o)
                    {
                        port = o.toString();
                        preference.setSummary(o.toString());
                        return true;
                    }
                });
            }

            Preference button = findPreference("obdConnect");
            if (button != null) {
                button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference arg0) {
                        if(OBDCONNECTED)
                        {
                            Toast.makeText(getActivity(), "Already connected.", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            connectToOdbReader();
                        }
                        return true;
                    }
                });
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
            int id = item.getItemId();
            if (id == android.R.id.home)
            {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        static String port;
        static String ip;
        static Socket sock;
        static String oem;

        private void connectToOdbReader()
        {
            ConnectTask connectTask = new ConnectTask();
            try
            {
                sock = connectTask.execute(ip, port).get();
                if(sock != null)
                {
                    OBDCONNECTED = true;
                    Intent i = new Intent("ALERTS_UPDATED");

                    Toast.makeText(getActivity(), "Connected.", Toast.LENGTH_SHORT).show();

                    i.putStringArrayListExtra("code", new TroubleCodeTask().execute(new TroubleCodesCommand()).get());
                    getActivity().sendBroadcast(i);
                }
                else
                {
                    OBDCONNECTED = false;
                }
            }
            catch(Exception e)
            {
                OBDCONNECTED = false;
            }
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("general_home_addr"));
            bindPreferenceSummaryToValue(findPreference("car_make"));

            Preference spotify = findPreference("spotifyLogout");
            if(spotify != null)
            {
                if(MainActivity.mPlayer != null && MainActivity.OAUTH != null)
                {
                    spotify.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                    {
                        @Override
                        public boolean onPreferenceClick(Preference preference)
                        {
                            MainActivity.mPlayer.logout();
                            return true;
                        }
                    });
                }
                else
                {
                    PreferenceScreen screen = getPreferenceScreen();
                    screen.removePreference(spotify);
                }
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
            int id = item.getItemId();
            if (id == android.R.id.home)
            {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}

class ConnectTask extends AsyncTask<String, Void, Socket>
{
    private static final int OBDTIMEOUT = 125;
    protected void onPreExecute(String... params)
    {
        connectionLoading.setTitle("OBD2 Connection  ");
        connectionLoading.setMessage("Connecting...");
        connectionLoading.show();
    }

    protected Socket doInBackground(String... params) {
        try
        {
            Log.d("CARDASH", "Connecting...");

            Socket sock = new Socket(InetAddress.getByName(params[0]), Integer.parseInt(params[1]));

            //Set up the OBD connection
            new ObdResetCommand().run(sock.getInputStream(), sock.getOutputStream());
            new EchoOffCommand().run(sock.getInputStream(), sock.getOutputStream());
            new LineFeedOffCommand().run(sock.getInputStream(), sock.getOutputStream());
            new TimeoutCommand(OBDTIMEOUT).run(sock.getInputStream(), sock.getOutputStream());
            SelectProtocolCommand protocolCommand = new SelectProtocolCommand(ObdProtocols.AUTO);
            protocolCommand.run(sock.getInputStream(), sock.getOutputStream());
            String protocol = protocolCommand.getFormattedResult();

            Log.d("CARDASH", "Connected.");

            return sock;
        }
        catch(Exception e)
        {
            return null;
        }
    }

    protected void onPostExecute(String... params) {
        // TODO: check this.exception
        // TODO: do something with the feed
        connectionLoading.dismiss();
    }
}

class TroubleCodeTask extends AsyncTask<TroubleCodesCommand, Void, ArrayList<String>>
{
    protected ArrayList<String> doInBackground(TroubleCodesCommand... params) {
        //TODO Sort this out
        String response = null;
        String[] codes = null;
        try
        {
            if(sock != null && SettingsActivity.OBDCONNECTED)
            {
                //Run command and get result
                params[0].run(sock.getInputStream(), sock.getOutputStream());
                response = params[0].getCalculatedResult();

                codes = response.split("\\n");
            }
        }
        catch(Exception e)
        {
            Log.e("CARDASH", e.getMessage());
        }
        return codes != null ? new ArrayList<>(Arrays.asList(codes)) : null;
    }

    protected void onPostExecute(String... params) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }
}
