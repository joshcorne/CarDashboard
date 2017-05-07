package uk.co.joshcorne.cardashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.joshcorne.cardashboard.models.Journey;
import uk.co.joshcorne.cardashboard.uk.co.joshcorne.cardashboard.adapters.JourneyActivity;
import uk.co.joshcorne.cardashboard.uk.co.joshcorne.cardashboard.adapters.StatsListAdapter;

public class StatsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.statistics_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        List<Journey> journeys = new ArrayList<>();//Select.from(Journey.class).list();
        journeys.add(new Journey(new Date()));
        journeys.get(0).setDistance(123.00);

        String[] rowDates = new String[journeys.size()];
        String[] rowDists = new String[journeys.size()];

        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy hh:mm");

        for (int i = 0; i < journeys.size(); i++)
        {
            rowDates[i] = dt.format(journeys.get(i).getDate());
            rowDists[i] = Double.toString(journeys.get(i).getDistance()) + " " +
                    PreferenceManager.getDefaultSharedPreferences(this).getString("general_distance_format", "miles");
        }

        ListView journeyListView = (ListView) findViewById(R.id.journeyList);
        StatsListAdapter listAdapter = new StatsListAdapter(this, rowDates, rowDists);
        if(journeyListView != null)
        {
            listAdapter.getCount();
            journeyListView.setAdapter(listAdapter);
            journeyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Intent i = new Intent(parent.getContext(), JourneyActivity.class);
                    i.putExtra("pos", position);
                    startActivity(i);
                }
            });
        }
    }

}
