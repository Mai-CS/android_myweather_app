package com.mai.myweather;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private GoogleMap mMap;
    private int mLocationsCount;
    private ArrayList<CustomLocation> mLocationsList;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private Button getWeatherButton;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Hawk.init(this).build();

        mLocationsList = Hawk.get(Constants.SAVED_LOCATIONS_LIST, null);
        if (mLocationsList == null)
        {
            mLocationsList = new ArrayList<>();
        }

        getWeatherButton = findViewById(R.id.get_weather_button);

        // obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.setRetainInstance(true);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // show a tutorial on how to add locations for the user
        if (Hawk.get(Constants.SAVED_IF_FIRST_LAUNCH, true))
        {
            Hawk.put(Constants.SAVED_IF_FIRST_LAUNCH, false);

            new ShowcaseView.Builder(this)
                    .withNewStyleShowcase()
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setTarget(new ViewTarget(R.id.search_layout, this))
                    .setContentTitle(getString(R.string.add_city))
                    .setContentText(getString(R.string.add_city_description))
                    .setShowcaseEventListener(new OnShowcaseEventListener()
                    {
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView)
                        {

                        }

                        @Override
                        public void onShowcaseViewDidHide(ShowcaseView showcaseView)
                        {
                            getWeatherButton.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onShowcaseViewShow(ShowcaseView showcaseView)
                        {

                        }

                        @Override
                        public void onShowcaseViewTouchBlocked(MotionEvent motionEvent)
                        {

                        }
                    })
                    .hideOnTouchOutside()
                    .build().show();
        }
        else
        {
            getWeatherButton.setVisibility(View.VISIBLE);
        }

        getWeatherButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // save added locations
                Hawk.put(Constants.SAVED_LOCATIONS_LIST, mLocationsList);

                // start fetch data
                startActivity(new Intent(MapsActivity.this, LocationsActivity.class));
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        showCurrentLocation(null);
    }


    /**
     * Add marker for the user's location on map
     */
    private void showCurrentLocation(Location currentLocation)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (Hawk.get(Constants.SAVED_IF_FIRST_PERMISSION_CHECK, true))
            {
                Hawk.put(Constants.SAVED_IF_FIRST_PERMISSION_CHECK, false);

                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.REQUEST_LOCATION_PERMISSION);
            }
            return;
        }

        if (currentLocation == null)
        {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>()
                    {
                        @Override
                        public void onSuccess(Location location)
                        {
                            // get last known location. In some rare situations this can be null.
                            if (location != null)
                            {
                                // add a marker on the current location and move the camera
                                LatLng currentLatLng =
                                        new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 5));
                            }

                            else
                            {
                                startLocationUpdates();
                            }

                            addMarkersOnMap();
                        }
                    });

            mFusedLocationClient.getLastLocation().addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    addMarkersOnMap();
                }
            });
        }

        else
        {
            // add a marker on the current location and move the camera
            LatLng currentLatLng =
                    new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 5));

            addMarkersOnMap();
        }
    }


    /**
     * Let the user add more locations on map
     */
    private void addMarkersOnMap()
    {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng)
            {
                mLocationsCount++;

                // add a marker on the selected location and move the camera
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.alpha(0.8f);
                markerOptions.title(String.valueOf(mLocationsCount));
                mMap.addMarker(markerOptions).showInfoWindow();

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
                mLocationsList.add(new CustomLocation(null, latLng, null, null));
            }
        });

        findViewById(R.id.search_layout).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    // prepare for a city search
                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                            .build();

                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .setFilter(typeFilter)
                                    .build(MapsActivity.this);
                    startActivityForResult(intent, Constants.REQUEST_PLACE_AUTOCOMPLETE);

                }
                catch (Exception e)
                {
                    // ignore the error.
                }
            }
        });

    }


    /**
     * Determine location update interval and make updates consume low power
     */
    private void createLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }


    private void startLocationUpdates()
    {
        createLocationRequest();
        if (mLocationRequest != null)
        {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);

            SettingsClient client = LocationServices.getSettingsClient(this);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
            task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>()
            {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse)
                {
                    // all location settings are satisfied.
                    // the client can initialize location requests here.
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapsActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        if (Hawk.get(Constants.SAVED_IF_FIRST_PERMISSION_CHECK, true))
                        {
                            Hawk.put(Constants.SAVED_IF_FIRST_PERMISSION_CHECK, false);

                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    Constants.REQUEST_LOCATION_PERMISSION);
                        }
                        return;
                    }

                    mLocationCallback = new LocationCallback()
                    {
                        @Override
                        public void onLocationResult(LocationResult locationResult)
                        {
                            if (locationResult == null)
                            {
                                return;
                            }
                            for (Location location : locationResult.getLocations())
                            {
                                // update UI with location data
                                if (location != null)
                                {
                                    showCurrentLocation(location);
                                    startLocationUpdates();
                                }
                            }
                        }
                    };

                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                }
            });

            task.addOnFailureListener(this, new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    if (e instanceof ResolvableApiException)
                    {
                        // location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try
                        {
                            // show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MapsActivity.this,
                                    Constants.REQUEST_CHECK_SETTINGS);
                        }
                        catch (IntentSender.SendIntentException sendEx)
                        {
                            // ignore the error.
                        }
                    }
                }
            });
        }
    }


    private void stopLocationUpdates()
    {
        if (mFusedLocationClient != null && mLocationCallback != null)
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case Constants.REQUEST_CHECK_SETTINGS:
            {
                if (resultCode == RESULT_OK)
                {
                    showCurrentLocation(null);
                }
                else
                {
                    addMarkersOnMap();
                }

                break;
            }

            case Constants.REQUEST_PLACE_AUTOCOMPLETE:
            {
                if (resultCode == RESULT_OK)
                {
                    Place place = PlaceAutocomplete.getPlace(this, data);

                    mLocationsCount++;

                    // add a marker on the selected location and move the camera
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(place.getLatLng());
                    markerOptions.alpha(0.8f);
                    markerOptions.title(String.valueOf(mLocationsCount));
                    mMap.addMarker(markerOptions).showInfoWindow();

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 5));
                    mLocationsList.add(new CustomLocation(place.getName().toString(),
                            place.getLatLng(), null, null));
                }

                break;
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case Constants.REQUEST_LOCATION_PERMISSION:
            {
                // if request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted
                    showCurrentLocation(null);
                }
                else
                {
                    addMarkersOnMap();
                }

                break;
            }
        }
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        stopLocationUpdates();
    }
}
