package com.meedamian.info;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CheckerService extends Service {
    public CheckerService() {}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new SimChecker(getApplicationContext());

        return super.onStartCommand(intent, flags, startId);
    }
}
