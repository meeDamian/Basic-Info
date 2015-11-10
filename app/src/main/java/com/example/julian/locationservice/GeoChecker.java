package com.example.julian.locationservice;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import permissions.dispatcher.PermissionUtils;

public class GeoChecker {

    public static final String PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;

    private static final int MISSING_PERMISSION = 1;

    public GeoChecker(Context c) {
        if (!PermissionUtils.hasSelfPermissions(c, PERMISSION)) showPermissionNotification(c);
        else cancelPermissionNotification(c);
    }

    public static void getLocation(Context c, LocationAvailabler la) {
        // ....

            //  ....


                if (la != null)
                    la.onLocationAvailable("Country", "City");
    }

    private NotificationManager getNotificationManager(Context c) {
        return (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    private void showPermissionNotification(Context c) {
        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(c)
                .setAutoCancel(true)
                .setSmallIcon(android.R.drawable.ic_secure)
                .setContentTitle("")
                .setContentText("")
                .setContentIntent(null);

        getNotificationManager(c).notify(MISSING_PERMISSION, mBuilder.build());
    }
    private void cancelPermissionNotification(Context c) {
        getNotificationManager(c).cancel(MISSING_PERMISSION);
    }


    public interface LocationAvailabler {
        void onLocationAvailable(String country, String city);
    }
}
