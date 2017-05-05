package uk.co.joshcorne.cardashboard.models;

/**
 * Created by josh on 3/9/17.
 */

public class Ping
{
    private double latitude;
    private double longitude;
    private float mpg;
    private double speed;
    private double altitude;
    private float fuelPressure;
    private float rpm;
    private float acceleration;
    private long time;
    private int id;

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

    public float getMpg()
    {
        return mpg;
    }

    public void setMpg(float mpg)
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

    public float getFuelPressure()
    {
        return fuelPressure;
    }

    public void setFuelPressure(float fuelPressure)
    {
        this.fuelPressure = fuelPressure;
    }

    public float getRpm()
    {
        return rpm;
    }

    public void setRpm(float rpm)
    {
        this.rpm = rpm;
    }

    public float getAcceleration()
    {
        return acceleration;
    }

    public void setAcceleration(float acceleration)
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

    public int getId()
    {
        return id;
    }
}
