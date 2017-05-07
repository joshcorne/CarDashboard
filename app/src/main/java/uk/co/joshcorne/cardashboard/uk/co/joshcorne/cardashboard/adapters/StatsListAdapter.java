package uk.co.joshcorne.cardashboard.uk.co.joshcorne.cardashboard.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import uk.co.joshcorne.cardashboard.R;

/**
 * Created by josh on 5/7/17.
 */

public class StatsListAdapter extends ArrayAdapter<String>
{
    private final Activity context;
    private final String[] date;
    private final String[] dist;

    public StatsListAdapter(Activity context,
                      String[] date, String[] dist) {
        super(context, R.layout.stats_row);
        this.context = context;
        this.date = date;
        this.dist = dist;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.stats_row, null, true);
        TextView date = (TextView) rowView.findViewById(R.id.date);
        TextView dist = (TextView) rowView.findViewById(R.id.dist);
        date.setText(this.date[position]);
        dist.setText(this.dist[position]);
        return rowView;
    }

    @Override
    public int getCount()
    {
        return date.length;
    }
}

