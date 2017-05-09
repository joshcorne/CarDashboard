package uk.co.joshcorne.cardashboard;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.orm.query.Select;

import java.text.SimpleDateFormat;
import java.util.List;

import uk.co.joshcorne.cardashboard.models.Journey;
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
        //TODO getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        List<Journey> journeys = Select.from(Journey.class).orderBy("id").list();

        String[] rowDates = new String[journeys.size()];
        String[] rowDists = new String[journeys.size()];

        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy hh:mm");

        for (int i = 0; i < journeys.size(); i++)
        {
            rowDates[i] = dt.format(journeys.get(i).getDate());
            rowDists[i] = String.format("%.2f", journeys.get(i).getDistanceInMiles()) + " " +
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
