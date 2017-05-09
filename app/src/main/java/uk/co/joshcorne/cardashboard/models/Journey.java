package uk.co.joshcorne.cardashboard.models;

import com.orm.SugarRecord;
import com.orm.dsl.Table;
import com.orm.query.Select;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by josh on 3/9/17.
 */

public class Journey extends SugarRecord
{
    private double distance = 0.0;
    private Date date;

    public Journey(){}

    public Journey(double distance, Date date)
    {
        this.distance = distance;
        this.date = date;
    }

    public Journey(Date date)
    {
        this.date = date;
    }

    public double getMaxSpeed()
    {
        double max = 0;
        for(Ping p : getPings())
        {
            if(max < p.getSpeed())
            {
                max = p.getSpeed();
            }
        }
        return max;
    }

    public int getMaxRevs()
    {
        int max = 0;
        for(Ping p : getPings())
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
        for(Ping p : getPings())
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
        for(Ping p : getPings())
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
        if(getPings().size() > 1)
        {
            for(Ping p : getPings())
            {
                cumulative += p.getSpeed();
            }
            return cumulative / getPings().size();
        }
        else
        {
            return 0;
        }
    }

    public int getAvgRevs()
    {
        int cumulative = 0;
        if(getPings().size() > 1)
        {
            for (Ping p : getPings())
            {
                cumulative += p.getRpm();
            }
            return cumulative / getPings().size();
        }
        else
        {
            return 0;
        }
    }

    public double getAvgConsumption()
    {
        float cumulative = 0;
        if(getPings().size() > 1)
        {
            for(Ping p : getPings())
            {
                cumulative += p.getMpg();
            }
            return cumulative / getPings().size();
        }
        else
        {
            return 0;
        }
    }

    public double getAvgPressure()
    {
        float cumulative = 0;
        if(getPings().size() > 1)
        {
            for(Ping p : getPings())
            {
                cumulative += p.getFuelPressure();
            }
            return cumulative / getPings().size();
        }
        else
        {
            return 0;
        }
    }

    public List<Ping> getPings()
    {
        return Ping.find(Ping.class, "journey_id = ?", Long.toString(getId()));
    }

    public Date getDate()
    {
        return date;
    }

    public String toString()
    {
        return getDate().toString();
    }

    public double getDistanceInMiles()
    {
        return distance * 0.000621371192;
    }

    public void setDistance(double distance)
    {
        this.distance = distance;
    }
}
