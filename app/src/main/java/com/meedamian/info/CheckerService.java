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

        new SimChecker(getApplicationContext());

        new GeoChecker(this, new GeoChecker.LocationAvailabler() {
            @Override
            public void onLocationAvailable(String country, String city) {

            // TODO: check for user-set replaces

            cacheLocally(country, city);

            uploadToParse(country, city);
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    private void cacheLocally(String country, String city) {
        BasicData.update(this, BasicData.COUNTRY, country);
        BasicData.update(this, BasicData.CITY, city);
    }

    private void uploadToParse(String country, String city) {
        new BasicData.Uploader(this)
            .setLocation(country, city)
            .upload();
    }
}
