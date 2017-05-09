package uk.co.joshcorne.cardashboard.uk.co.joshcorne.cardashboard.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import uk.co.joshcorne.cardashboard.R;

/**
 * Created by josh on 5/7/17.
 */

public class AlertsListAdapter extends ArrayAdapter<String>
{
    private final Activity context;
    private final String[] code;
    private final String[] desc;

    public AlertsListAdapter(Activity context,
                             String[] code, String[] desc) {
        super(context, R.layout.stats_row);
        this.context = context;
        this.code = code;
        this.desc = desc;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.alerts_row, null, true);
        TextView code = (TextView) rowView.findViewById(R.id.code);
        TextView desc = (TextView) rowView.findViewById(R.id.desc);
        code.setText(this.code[position]);
        desc.setText(this.desc[position]);
        return rowView;
    }

    @Override
    public int getCount()
    {
        return code.length;
    }
}

