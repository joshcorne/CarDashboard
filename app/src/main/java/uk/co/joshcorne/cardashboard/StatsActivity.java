package uk.co.joshcorne.cardashboard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.joshcorne.cardashboard.models.Journey;

public class StatsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Statistics");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        List<String> journeys = new ArrayList<>();//Select.from(Journey.class).list();
        journeys.add(String.valueOf(new Date()));

        ListView journeyListView = (ListView) findViewById(R.id.journeyList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.content_stats, journeys);
        if(journeyListView != null)
        {
            journeyListView.setAdapter(arrayAdapter);
        }
    }

}
