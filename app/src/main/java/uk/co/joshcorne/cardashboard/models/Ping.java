package uk.co.joshcorne.cardashboard.models;

import com.orm.SugarRecord;

/**
 * Created by josh on 3/9/17.
 */

public class Ping extends SugarRecord
{
    private double latitude;
    private double longitude;
    private double mpg;
    private double speed;
    private double altitude;
    private double fuelPressure;
    private double rpm;
    private double acceleration;
    private long time;

    public Ping()
    {
        setTime(System.currentTimeMillis());
    }

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public double getMpg()
    {
        return mpg;
    }

    public void setMpg(double mpg)
    {
        this.mpg = mpg;
    }

    public double getSpeed()
    {
        return speed;
    }

    public void setSpeed(double speed)
    {
        this.speed = speed;
    }

    public double getAltitude()
    {
        return altitude;
    }

    public void setAltitude(double altitude)
    {
        this.altitude = altitude;
    }

    public double getFuelPressure()
    {
        return fuelPressure;
    }

    public void setFuelPressure(double fuelPressure)
    {
        this.fuelPressure = fuelPressure;
    }

    public double getRpm()
    {
        return rpm;
    }

    public void setRpm(double rpm)
    {
        this.rpm = rpm;
    }

    public double getAcceleration()
    {
        return acceleration;
    }

    public void setAcceleration(double acceleration)
    {
        this.acceleration = acceleration;
    }

    public long getTime()
    {
        return time;
    }

    public void setTime(long time)
    {
        this.time = time;
    }
}
