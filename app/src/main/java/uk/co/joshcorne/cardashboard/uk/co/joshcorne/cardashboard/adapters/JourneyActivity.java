package uk.co.joshcorne.cardashboard.uk.co.joshcorne.cardashboard.adapters;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.joshcorne.cardashboard.R;
import uk.co.joshcorne.cardashboard.models.Journey;
import uk.co.joshcorne.cardashboard.models.Ping;

public class JourneyActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);
        setTitle(getString(R.string.journey_statistics_title));

        Intent i = getIntent();

        TextView avgRevs = (TextView) findViewById(R.id.avg_revs);
        TextView avgConsumption = (TextView) findViewById(R.id.avg_consumption);
        TextView avgSpeed = (TextView) findViewById(R.id.avg_speed);
        TextView avgPressure = (TextView) findViewById(R.id.avg_pressure);

        TextView maxRevs = (TextView) findViewById(R.id.max_revs);
        TextView maxConsumption = (TextView) findViewById(R.id.max_consumption);
        TextView maxSpeed = (TextView) findViewById(R.id.max_speed);
        TextView maxPressure = (TextView) findViewById(R.id.max_pressure);

        TextView dateTitle = (TextView) findViewById(R.id.journey_date);

        List<Journey> db = new ArrayList<>();//Journey.find(Journey.class, "some = ?", String.valueOf(i.getIntExtra("pos", 0)));
        Ping p = new Ping();
        p.setSpeed(1.1);
        p.setMpg(1.1);
        p.setRpm(1);
        p.setFuelPressure(1.1);
        Journey a = new Journey(new Date());
        a.addPing(p);
        db.add(a);

        if(db.size() > 0 && dateTitle != null
                && avgSpeed != null && avgConsumption != null && avgRevs != null && avgPressure != null
                && maxSpeed != null && maxConsumption != null && maxRevs != null && maxPressure != null)
        {
            Journey j = db.get(0);

            dateTitle.setText(j.getDate().toString());

            avgRevs.setText(String.valueOf(j.getAvgRevs()));
            avgConsumption.setText(String.valueOf(j.getAvgConsumption()));
            avgSpeed.setText(String.valueOf(j.getAvgSpeed()));
            avgPressure.setText(String.valueOf(j.getAvgPressure()));

            maxRevs.setText(String.valueOf(j.getMaxRevs()));
            maxConsumption.setText(String.valueOf(j.getMaxConsumption()));
            maxSpeed.setText(String.valueOf(j.getMaxSpeed()));
            maxPressure.setText(String.valueOf(j.getMaxPressure()));
        }
    }
}
