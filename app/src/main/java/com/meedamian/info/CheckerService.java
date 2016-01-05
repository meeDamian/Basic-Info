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

        new GeoChecker(this)
            .getNewLocation(new GeoChecker.LocationAvailabler() {
                @Override
                public void onLocationAvailable(String country, String city) {
                if (country != null && city != null)
                    LocalData.saveLocation(CheckerService.this, country, city);
                }
            });

        return super.onStartCommand(intent, flags, startId);
    }
}
