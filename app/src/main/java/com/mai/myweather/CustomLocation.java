package com.mai.myweather;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Mai on 7/19/2018.
 */

@SuppressWarnings({"unused", "DefaultFileTemplate", "WeakerAccess"})
public class CustomLocation
{
    private String name;
    private LatLng latLng;
    private String status;
    private String temp;

    public CustomLocation(String name, LatLng latLng, String status, String temp)
    {
        this.setName(name);
        this.setLatLng(latLng);
        this.setStatus(status);
        this.setTemp(temp);
    }

    public String getName()
    {
        if (name != null && !name.isEmpty())
            return name;
        else
            return "Unknown City";
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public LatLng getLatLng()
    {
        return latLng;
    }

    public void setLatLng(LatLng latLng)
    {
        this.latLng = latLng;
    }

    public String getStatus()
    {
        if (status != null && !status.isEmpty())
            return status;
        else
            return "Can't determine the weather status";
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getTemp()
    {
        if (temp != null && !temp.isEmpty())
            return temp;
        else
            return "N/A";
    }

    public void setTemp(String temp)
    {
        this.temp = temp;
    }
}
