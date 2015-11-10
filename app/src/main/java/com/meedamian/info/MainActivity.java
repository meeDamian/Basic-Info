package com.meedamian.info;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    /*
    * phone
    * phone updated
    *
    * location
    * location updated
    * url
    * hash
    */

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

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    protected void initGeo() {
        Log.w("Basic Info", "@Rasmuhamed haven't implemented it yet");
    }
}