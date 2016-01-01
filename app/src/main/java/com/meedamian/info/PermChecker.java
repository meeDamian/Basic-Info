package com.meedamian.info;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;

import permissions.dispatcher.PermissionUtils;

public abstract class PermChecker {

    public static final int MISSING_LOCATION_PERM   = 111;
    public static final int MISSING_PHONE_PERM      = 222;

    protected boolean isPermitted(Context c) {
        if (!PermissionUtils.hasSelfPermissions(c, getPermission())) {
            showPermissionNotification(c);
            return false;
        }

        cancelPermissionNotification(c);
        return true;
    }

    protected abstract int getNotificationId();
    protected abstract String getPermission();
    protected abstract @DrawableRes int getSmallIcon();
    protected abstract @StringRes   int getText();


    private void showPermissionNotification(@NonNull Context c) {
        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(c)
                .setSmallIcon(getSmallIcon())
                .setContentTitle(c.getString(R.string.permission_missing))
                .setContentText(c.getString(getText()))

                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_ERROR)

                .addAction(R.drawable.ic_lightbulb_outline_24dp, c.getString(R.string.permission_action_text), getSettingsPendingIntent(c))
                .setContentIntent(getAppPendingIntent(c));

        getNotificationManager(c).notify(getNotificationId(), mBuilder.build());
    }
    private void cancelPermissionNotification(@NonNull Context c) {
        getNotificationManager(c).cancel(getNotificationId());
    }


    protected NotificationManager getNotificationManager(@NonNull Context c) {
        return (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
    }


    private PendingIntent getSettingsPendingIntent(@NonNull Context c) {
        return PendingIntent.getActivity(c, 0,
            new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", c.getPackageName(), null)
            ),
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    }
    protected PendingIntent getAppPendingIntent(@NonNull Context c) {
        return PendingIntent.getActivity(c, 0,
            new Intent(
                c,
                MainActivity.class
            ),
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    }
}
