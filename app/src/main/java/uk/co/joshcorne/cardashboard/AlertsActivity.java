package uk.co.joshcorne.cardashboard;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.orm.SugarApp;
import com.orm.SugarContext;
import com.orm.SugarDb;
import com.orm.query.Select;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import uk.co.joshcorne.cardashboard.models.TroubleCode;
import uk.co.joshcorne.cardashboard.uk.co.joshcorne.cardashboard.adapters.StatsListAdapter;

import static uk.co.joshcorne.cardashboard.SettingsActivity.ObdPreferenceFragment.sock;

public class AlertsActivity extends AppCompatActivity
{
    List<String> receivedCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.alerts_title));
        //TODO getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        receivedCodes = getIntent().getStringArrayListExtra("codes");
        String[] receivedDescs = new String[receivedCodes.size()];

        TroubleCode troubleCode;
        try
        {
            if(sock != null && SettingsActivity.OBDCONNECTED)
            {
                //Make sure they have set the oem
                String oem = PreferenceManager.getDefaultSharedPreferences(this).getString("car_make", "unset");
                if(!oem.equals("unset"))
                {
                    for (int i = 0; i < receivedCodes.size(); i++)
                    {
                        try
                        {
                            //Find a specific code description
                            List<TroubleCode> select = TroubleCode.find(TroubleCode.class, "dtc_key = ? and dtc_make = ?", receivedCodes.get(i), oem);
                            if (select.size() < 1)
                            {
                                //Otherwise find a generic
                                select = TroubleCode.find(TroubleCode.class, "dtc_key = ? and dtc_make = ?", receivedCodes.get(i), "Generic");
                                if (select.size() < 1)
                                {
                                    //No code found
                                    throw new Exception("Code not found.");
                                }
                                else
                                {
                                    troubleCode = select.get(0);
                                }
                            }
                            else
                            {
                                troubleCode = select.get(0);
                            }
                            receivedDescs[i] = troubleCode.getDtcValue();
                        }
                        catch (Exception e)
                        {
                            receivedDescs[i] = e.getMessage();
                        }
                    }
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

        ListView alertListView = (ListView) findViewById(R.id.alerts_desc_list);
        StatsListAdapter listAdapter = new StatsListAdapter(this, receivedCodes.toArray(new String[receivedCodes.size()]), receivedDescs);
        if(alertListView != null)
        {
            listAdapter.getCount();
            alertListView.setAdapter(listAdapter);
        }
    }
}
