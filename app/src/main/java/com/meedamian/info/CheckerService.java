package com.meedamian.info;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.julian.locationservice.GeoChecker;

import java.util.HashSet;
import java.util.Set;

public class CheckerService extends Service {
    public CheckerService() {}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new SimChecker(getApplicationContext());

        new GeoChecker(this, new GeoChecker.LocationAvailabler() {
            @Override
            public void onLocationAvailable(String country, String city) {
            Set<String> locationSet = new HashSet<>();
            locationSet.add(country);
            locationSet.add(city);
            BasicData.update(CheckerService.this, BasicData.LOCATION, locationSet);

            uploadToParse(country, city);
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    private void uploadToParse(String country, String city) {
        Log.d("Basic Data", "Uploading to parse (" + country + ", " + city + ")");
    }
}
