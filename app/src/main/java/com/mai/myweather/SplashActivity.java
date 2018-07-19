package com.mai.myweather;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.orhanobut.hawk.Hawk;


public class SplashActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Hawk.init(this).build();

        if (Hawk.get(Constants.SAVED_IF_FIRST_LAUNCH, true))
            launchActivityWithoutBack(MapsActivity.class);

        else
            launchActivityWithoutBack(LocationsActivity.class);
    }


    /**
     * Prevent going back to SplashActivity
     *
     * @param classDestination destination activity (example: MainActivity.class)
     */
    private void launchActivityWithoutBack(Class classDestination)
    {
        Intent intent = new Intent(SplashActivity.this, classDestination);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
