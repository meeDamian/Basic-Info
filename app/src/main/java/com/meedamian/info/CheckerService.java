package com.meedamian.info;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;

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
                public void onLocationAvailable(@NonNull BasicData bd) {
                if (bd.country != null && bd.city != null)
                    LocalData.saveLocation(CheckerService.this, bd.country, bd.city);
                }
            });

        return super.onStartCommand(intent, flags, startId);
    }
}
