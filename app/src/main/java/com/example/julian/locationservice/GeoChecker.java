package com.example.julian.locationservice;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.meedamian.info.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import permissions.dispatcher.PermissionUtils;

public class GeoChecker implements
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private static final int MISSING_PERMISSION = 1;

    private GoogleApiClient mGoogleApiClient;
    private LocationAvailabler la;
    private Context c;


    public GeoChecker(Context context, LocationAvailabler locationer) {
        la = locationer;
        c = context;

        buildGoogleApiClient(c).connect();

        if (!PermissionUtils.hasSelfPermissions(c, PERMISSION)) showPermissionNotification(c);
        else cancelPermissionNotification(c);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            Address address = null;

            try {
                List<Address> addresses = new Geocoder(c, Locale.getDefault()).getFromLocation(
                    mLastLocation.getLatitude(),
                    mLastLocation.getLongitude(),
                    1
                );
                address = addresses.get(0);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (address != null && la != null)
                la.onLocationAvailable(
                    address.getCountryName(),
                    address.getLocality()
                );
        }
    }
    public GeoChecker(Context c) { this(c,null); }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Basic Info", "suspended");

    }

    public void locationQuery(String country, String city, GoogleMap googleMap){
        String locationQuery = null;
        if (country != null)
            locationQuery = country;

        if (city != null) {
            if (locationQuery == null)
                locationQuery = city;
            else
                locationQuery += ", " + city;
        }

        if (locationQuery != null) {
            try {
                Address address = new Geocoder(c).getFromLocationName(locationQuery, 1).get(0);
                LatLng position = new LatLng(
                        address.getLatitude(),
                        address.getLongitude()
                );

                googleMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(country + ", " + city));

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 11));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("Basic Info", "failed");

    }

    protected synchronized GoogleApiClient buildGoogleApiClient(Context c) {
        return mGoogleApiClient = new GoogleApiClient.Builder(c)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
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
                .setContentTitle(c.getString(R.string.location_permission_title))
                .setContentText(c.getString(R.string.location_permission_content_text))
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
