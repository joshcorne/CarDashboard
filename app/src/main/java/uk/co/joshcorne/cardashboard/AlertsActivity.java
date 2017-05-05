package uk.co.joshcorne.cardashboard;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.github.pires.obd.commands.control.TroubleCodesCommand;

import java.io.IOException;
import java.util.List;

import uk.co.joshcorne.cardashboard.models.TroubleCode;

import static uk.co.joshcorne.cardashboard.SettingsActivity.GeneralPreferenceFragment.sock;

public class AlertsActivity extends AppCompatActivity
{
    List<TroubleCode> receivedCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Alerts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try
        {
            if(sock != null && SettingsActivity.OBDCONNECTED)
            {
                //Run command and get result
                TroubleCodesCommand troubleCodesCommand = new TroubleCodesCommand();
                troubleCodesCommand.run(sock.getInputStream(), sock.getOutputStream());
                String response = troubleCodesCommand.getFormattedResult();

                //Make sure they have set the oem
                String oem = PreferenceManager.getDefaultSharedPreferences(this).getString("car_make", "unset");
                if(!oem.equals("unset"))
                {
                    //Find a specific code description
                    List<TroubleCode> troubleCodes = TroubleCode.find(TroubleCode.class, "dtc_key = ? and dtc_make = ?", response, oem);
                    if(troubleCodes.isEmpty())
                    {
                        //Otherwise find a generic
                        troubleCodes = TroubleCode.find(TroubleCode.class, "dtc_key = ? and dtc_make = ?", response, "Generic");
                        if(troubleCodes.isEmpty())
                        {
                            //No code found
                            throw new Exception("Code not found.");
                        }
                    }
                    //Return code
                    receivedCodes.add(troubleCodes.get(0));
                }
                else
                {
                    Toast.makeText(this, "Please set your make of car.", Toast.LENGTH_SHORT).show();
                }
            }

        }
        catch(Exception e)
        {
            Toast.makeText(this, "Code lookup failed.", Toast.LENGTH_SHORT).show();
        }
    }
}
