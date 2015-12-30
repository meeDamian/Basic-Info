package com.meedamian.info;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.julian.locationservice.GeoChecker;

public class CheckerService extends Service {
    public CheckerService() {}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new SimChecker(this);

        new GeoChecker(this, new GeoChecker.LocationAvailabler() {
            @Override
            public void onLocationAvailable(String country, String city) {
            save(country, city);
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    private void save(String country, String city) {
        BasicData.getInstance(this)
            .setLocation(country, city)
            .save()
            .upload();
    }
}
