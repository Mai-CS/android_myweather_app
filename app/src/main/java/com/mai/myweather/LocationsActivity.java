package com.mai.myweather;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

public class LocationsActivity extends AppCompatActivity
{
    private ArrayList<CustomLocation> mLocationsList;
    private RecyclerView mRecyclerView;

    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);

        Hawk.init(this).build();

        mLocationsList = Hawk.get(Constants.SAVED_LOCATIONS_LIST, null);
        if (mLocationsList == null)
            mLocationsList = new ArrayList<>();

        mRecyclerView = findViewById(R.id.locations_recycler_view);
        emptyView = findViewById(R.id.empty_view);
        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        Button addLocationButton = findViewById(R.id.add_location_button);

        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchData();

        /*
         * sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener()
                {
                    @Override
                    public void onRefresh()
                    {
                        fetchData();
                    }
                }
        );

        addLocationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(LocationsActivity.this, MapsActivity.class));
            }
        });

    }


    /**
     * Generate urls for each location to get weather forecast
     */
    private void fetchData()
    {
        if (mLocationsList != null && !mLocationsList.isEmpty())
        {
            swipeRefreshLayout.setRefreshing(true);
            emptyView.setVisibility(View.GONE);

            ArrayList<String> requestsList = new ArrayList<>();
            for (CustomLocation customLocation : mLocationsList)
            {
                HashMap<String, String> paramsMap = new HashMap<>();
                paramsMap.put("appid", Constants.WEATHER_API_KEY);
                paramsMap.put("units", "metric");
                paramsMap.put("lat", String.valueOf(customLocation.getLatLng().latitude));
                paramsMap.put("lon", String.valueOf(customLocation.getLatLng().longitude));

                requestsList.add(NetworkingJob.generateUrl(Constants.WEATHER_API_BASE_URL, paramsMap));
            }
            new NetworkingJob(this).runJob(requestsList);
        }
        else
        {
            swipeRefreshLayout.setRefreshing(false);
            emptyView.setVisibility(View.VISIBLE);
        }
    }


    /**
     * When NetworkingJob completes its task, this method will be invoked to handle json result
     *
     * @param event contains the json result
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onNetworkingEvent(NetworkingEvent event)
    {
        swipeRefreshLayout.setRefreshing(false);

        if (event.getResultStatus() == NetworkingEvent.ResultStatus.SUCCESS)
        {
            if (event.getJsonResults() != null)
            {
                if (mLocationsList != null)
                    mLocationsList.clear();

                for (JsonObject jsonObject : event.getJsonResults())
                {
                    if (jsonObject != null)
                    {
                        // Parse json
                        double temp = jsonObject.get("main").getAsJsonObject().get("temp").getAsDouble();
                        int roundedTemp = (int) Math.round(temp);

                        String name = jsonObject.get("name").getAsString() + ", " +
                                jsonObject.get("sys").getAsJsonObject().get("country").getAsString();

                        String status = jsonObject.get("weather").getAsJsonArray()
                                .get(0).getAsJsonObject().get("main").getAsString();

                        double lat = jsonObject.get("coord").getAsJsonObject().get("lat").getAsDouble();
                        double lng = jsonObject.get("coord").getAsJsonObject().get("lon").getAsDouble();

                        CustomLocation customLocation = new CustomLocation(
                                name,
                                new LatLng(lat, lng),
                                status,
                                String.valueOf(roundedTemp));
                        if (mLocationsList != null)
                        {
                            mLocationsList.add(customLocation);
                        }
                    }
                }

                // specify an adapter
                mRecyclerView.setAdapter(new LocationsAdapter(mLocationsList));
            }
        }
        else if (event.getResultStatus() == NetworkingEvent.ResultStatus.OFFLINE)
        {
            Toast.makeText(this, R.string.offline_error, Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }

        EventBus.getDefault().removeStickyEvent(event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_locations, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_refresh:
                fetchData();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        EventBus.getDefault().register(this);
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
