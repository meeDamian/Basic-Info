package com.meedamian.info;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;

import permissions.dispatcher.PermissionUtils;

public class SimChecker {

    public static final String PERMISSION = Manifest.permission.READ_PHONE_STATE;

    private static final int PHONE_CHANGED_NOTIFICATION_ID      = 1;
    private static final int PERMISSION_MISSING_NOTIFICATION_ID = 2;

    public SimChecker(Context c) {
        if (!PermissionUtils.hasSelfPermissions(c, PERMISSION)) {
            showPermissionNotification(c);
            return;

        } else
            cancelPermissionNotification(c);

        String currentSubscriber = getCurrentSubscriberId(c);
        if (currentSubscriber == null)
            return; // There's no SIM present - ignore

        String cachedSubscriber = getCachedSubscriberId(c);
        if (cachedSubscriber == null) {
            cacheNewSubscriber(c, currentSubscriber);
            return;
        }

        if (!cachedSubscriber.equals(currentSubscriber)) {
            cacheNewSubscriber(c, currentSubscriber);
            showSimChangedNotification(c);
        }
    }

    private NotificationManager getNotificationManager(Context c) {
        return (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private PendingIntent getAppPendingIntent(Context c) {
        return PendingIntent.getActivity(c, 0,
            new Intent(
                c,
                MainActivity.class
            ),
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    }
    private PendingIntent getSettingsPendingIntent(Context c) {
        return PendingIntent.getActivity(c, 0,
            new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", c.getPackageName(), null)
            ),
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private void showSimChangedNotification(Context c) {
        String phoneNo = BasicData.getString(c, BasicData.PHONE_NO);

        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(c)
                .setSmallIcon(android.R.drawable.ic_secure)
                .setContentTitle("SIM card changed")
                .setContentText("Your current phone number is " + phoneNo)
                .addAction(android.R.drawable.ic_menu_save, "Correct", null)
                .addAction(android.R.drawable.ic_secure, "Change", getAppPendingIntent(c))
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setContentIntent(getAppPendingIntent(c));

        getNotificationManager(c).notify(PHONE_CHANGED_NOTIFICATION_ID, mBuilder.build());
    }

    private void showPermissionNotification(Context c) {
        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(c)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setSmallIcon(android.R.drawable.ic_secure)
                .setContentTitle("Permission missing")
                .setContentText("Phone number change notifications disabled")
                .addAction(android.R.drawable.ic_secure, "Enable", getAppPendingIntent(c))
                .addAction(android.R.drawable.ic_secure, "Ignore", null)
                .setContentIntent(getSettingsPendingIntent(c));

        getNotificationManager(c).notify(PERMISSION_MISSING_NOTIFICATION_ID, mBuilder.build());
    }
    private void cancelPermissionNotification(Context c) {
        getNotificationManager(c).cancel(PERMISSION_MISSING_NOTIFICATION_ID);
    }

    private String getCachedSubscriberId(Context c) {
        return BasicData.getString(c, BasicData.SUBSCRIBER_ID);
    }
    private void cacheNewSubscriber(Context c, String newSubscriberId) {
        BasicData.update(c, BasicData.SUBSCRIBER_ID, newSubscriberId);
    }

    private String getCurrentSubscriberId(Context c) {
        TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSubscriberId();
    }
}
