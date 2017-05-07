package uk.co.joshcorne.cardashboard.models;

import com.orm.SugarRecord;
import com.orm.dsl.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by josh on 3/9/17.
 */

public class Journey extends SugarRecord
{
    private List<Ping> pings = new ArrayList<>();
    private double distance;
    private Date date;

    public Journey(){}

    public Journey(double distance, List<Ping> pings, Date date)
    {
        this.distance = distance;
        this.pings = pings;
        this.date = date;
    }

    public Journey(Date date)
    {
        this.date = date;
    }

    public double getMaxSpeed()
    {
        double max = 0;
        for(Ping p : pings)
        {
            if(max < p.getSpeed())
            {
                max = p.getSpeed();
            }
        }
        return max;
    }

    public double getMaxRevs()
    {
        double max = 0;
        for(Ping p : pings)
        {
            if(max < p.getRpm())
            {
                max = p.getRpm();
            }
        }
        return max;
    }

    public double getMaxConsumption()
    {
        double max = 0;
        for(Ping p : pings)
        {
            if(max < p.getMpg())
            {
                max = p.getMpg();
            }
        }
        return max;
    }

    public double getMaxPressure()
    {
        double max = 0;
        for(Ping p : pings)
        {
            if(max < p.getFuelPressure())
            {
                max = p.getFuelPressure();
            }
        }
        return max;
    }

    public double getAvgSpeed()
    {
        float cumulative = 0;
        for(Ping p : pings)
        {
            cumulative += p.getSpeed();
        }
        return cumulative / pings.size();
    }

    public double getAvgRevs()
    {
        float cumulative = 0;
        for(Ping p : pings)
        {
            cumulative += p.getRpm();
        }
        return cumulative / pings.size();

    }

    public double getAvgConsumption()
    {
        float cumulative = 0;
        for(Ping p : pings)
        {
            cumulative += p.getMpg();
        }
        return cumulative / pings.size();

    }

    public double getAvgPressure()
    {
        float cumulative = 0;
        for(Ping p : pings)
        {
            cumulative += p.getFuelPressure();
        }
        return cumulative / pings.size();
    }

    public List<Ping> getPings()
    {
        return pings;
    }

    public void addPing(Ping ping)
    {
        this.pings.add(ping);
    }
    public void addPings(List<Ping> pings)
    {
        this.pings.addAll(pings);
    }

    public Date getDate()
    {
        return date;
    }

    public String toString()
    {
        return getDate().toString();
    }

    public double getDistance()
    {
        return distance;
    }

    public void setDistance(double distance)
    {
        this.distance = distance;
    }
}
