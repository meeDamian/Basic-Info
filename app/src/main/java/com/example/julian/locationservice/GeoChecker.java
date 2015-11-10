package com.example.julian.locationservice;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import permissions.dispatcher.PermissionUtils;

public class GeoChecker {

    public static final String PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;

    private static final int MISSING_PERMISSION = 1;

    static GoogleApiClient mGoogleApiClient;
    static Location mLocation;

    public GeoChecker(Context c) {
        if (!PermissionUtils.hasSelfPermissions(c, PERMISSION)) showPermissionNotification(c);
        else cancelPermissionNotification(c);
    }

    public static void getLocation(Context c, LocationAvailabler la) {
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Geocoder geocoder = new Geocoder(c, Locale.getDefault());
        List<Address> addresses = null;
        String country = null;
        String city = null;

        try{
            addresses = geocoder.getFromLocation(
                    mLocation.getLatitude(),
                    mLocation.getLongitude(),
                    1
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(addresses != null || addresses.size() != 0){
            Address address = addresses.get(0);
            country = address.getCountryName();
            city = address.getLocality();
        }

        if (la != null)
            la.onLocationAvailable(country, city);
    }



    protected synchronized void buildGoogleApiClient(Context c) {
        mGoogleApiClient = new GoogleApiClient.Builder(c)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) c)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) c)
                .addApi(LocationServices.API)
                .build();
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
