package com.mai.myweather;

import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * Created by Mai on 7/19/2018.
 */

@SuppressWarnings({"DefaultFileTemplate", "WeakerAccess", "unused"})
public class NetworkingEvent
{
    enum ResultStatus
    {
        SUCCESS,
        ERROR,
        OFFLINE
    }

    private ResultStatus resultStatus;
    private ArrayList<JsonObject> jsonResults = new ArrayList<>();

    public NetworkingEvent(ResultStatus resultStatus, ArrayList<JsonObject> jsonResults)
    {
        this.resultStatus = resultStatus;
        this.jsonResults = jsonResults;
    }

    public ResultStatus getResultStatus()
    {
        return resultStatus;
    }

    public ArrayList<JsonObject> getJsonResults()
    {
        return jsonResults;
    }
}
