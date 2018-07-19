package com.mai.myweather;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mai on 7/19/2018.
 */

@SuppressWarnings({"DefaultFileTemplate", "WeakerAccess"})
public class NetworkingJob
{
    private Context mContext;
    private ArrayList<String> mRequestsList = new ArrayList<>();
    private ArrayList<JsonObject> mJsonResults = new ArrayList<>();


    public NetworkingJob(Context context)
    {
        this.mContext = context;
    }


    /**
     * @param requestsList contains all urls
     */
    public void runJob(ArrayList<String> requestsList)
    {
        if (requestsList != null && !requestsList.isEmpty())
        {
            this.mRequestsList = requestsList;

            for (String requestUrl : requestsList)
            {
                requestData(requestUrl);
            }
        }
    }


    /**
     * Send request to server
     *
     * @param url should include the url and its params
     */
    public void requestData(final String url)
    {
        try
        {
            if (isOnline(mContext))
            {
                Ion.with(mContext)
                        .load("GET", url)
                        .setTimeout(20000)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>()
                        {
                            @Override
                            public void onCompleted(Exception e, JsonObject result)
                            {
                                mJsonResults.add(result);

                                if (mRequestsList.size() != 0 && mRequestsList.size() == mJsonResults.size())
                                    EventBus.getDefault().postSticky(
                                            new NetworkingEvent(NetworkingEvent.ResultStatus.SUCCESS, mJsonResults));
                            }
                        });
            }
            else
            {
                EventBus.getDefault().postSticky(
                        new NetworkingEvent(NetworkingEvent.ResultStatus.OFFLINE, null));
            }
        }
        catch (Exception ex)
        {
            EventBus.getDefault().postSticky(
                    new NetworkingEvent(NetworkingEvent.ResultStatus.ERROR, null));
        }
    }


    /**
     * Add the required parameters to url
     *
     * @param paramsValues contains the parameters keys and values
     * @return the complete url
     */
    public static String generateUrl(String url, HashMap<String, String> paramsValues)
    {
        String paramsString = "";
        StringBuilder stringBuilder = new StringBuilder(paramsString);
        int i = 1;
        for (Map.Entry<String, String> entry : paramsValues.entrySet())
        {
            String key;
            String value;
            try
            {
                key = URLEncoder.encode(entry.getKey(), "UTF-8");
                if (entry.getValue() != null)
                {
                    value = URLEncoder.encode(entry.getValue(), "UTF-8");
                }
                else
                {
                    value = entry.getValue();
                }

                if (i == 1)
                {
                    stringBuilder.append("?").append(key).append("=").append(value);
                }
                else
                {
                    stringBuilder.append("&").append(key).append("=").append(value);
                }
            }
            catch (UnsupportedEncodingException ex)
            {
                ex.printStackTrace();
            }

            i++;
        }

        return url + stringBuilder.toString();
    }


    /**
     * Check whether the device is offline or online
     *
     * @return connectivity status
     */
    public static boolean isOnline(Context context)
    {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
        {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnectedOrConnecting();
        }
        else
        {
            return false;
        }
    }
}
