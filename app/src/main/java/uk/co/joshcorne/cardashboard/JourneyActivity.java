package uk.co.joshcorne.cardashboard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.orm.query.Select;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy hh:mm");

        Journey j = Select.from(Journey.class).orderBy("id").list().get(i.getIntExtra("pos", 0));

        if(j != null && dateTitle != null
                && avgSpeed != null && avgConsumption != null && avgRevs != null && avgPressure != null
                && maxSpeed != null && maxConsumption != null && maxRevs != null && maxPressure != null)
        {
            dateTitle.setText(dt.format(j.getDate()));

            avgRevs.setText(getString(R.string.journey_avg_revs) + " " + String.valueOf(j.getAvgRevs()) + "RPM");
            avgConsumption.setText(getString(R.string.journey_avg_consumption) + " " + String.format("%.2f", j.getAvgConsumption()) + " litres per hour");
            avgSpeed.setText(getString(R.string.journey_avg_speed) + " " + String.format("%.2f", j.getAvgSpeed()) + "MPH");
            avgPressure.setText(getString(R.string.journey_avg_pressure) + " " + String.format("%.2f", j.getAvgPressure()) + "kPa");

            maxRevs.setText(getString(R.string.journey_max_revs) + " " + String.valueOf(j.getMaxRevs()) + "RPM");
            maxConsumption.setText(getString(R.string.journey_max_consumption) + " " + String.format("%.2f", j.getMaxConsumption()) + " litres per hour");
            maxSpeed.setText(getString(R.string.journey_max_speed) + " " + String.format("%.2f", j.getMaxSpeed()) + "MPH");
            maxPressure.setText(getString(R.string.journey_max_pressure) + " " + String.format("%.2f", j.getMaxPressure()) + "kPa");
        }
    }
}
