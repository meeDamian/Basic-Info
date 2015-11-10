package com.meedamian.info;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.julian.locationservice.GeoChecker;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void init() {
        Receiver.setAlarm(this);
        MainActivityPermissionsDispatcher.initSimWithCheck(this);
        MainActivityPermissionsDispatcher.initGeoWithCheck(this);
    }

    @NeedsPermission(SimChecker.PERMISSION)
    protected void initSim() {
        new SimChecker(this);
    }

    @NeedsPermission(GeoChecker.PERMISSION)
    protected void initGeo() {
        final TextView tv = (TextView) findViewById(R.id.location);
        GeoChecker.getLocation(this, new GeoChecker.LocationAvailabler() {
            @Override
            public void onLocationAvailable(String country, String city) {
                tv.setText(country + ", " + city);
            }
        });
    }
}