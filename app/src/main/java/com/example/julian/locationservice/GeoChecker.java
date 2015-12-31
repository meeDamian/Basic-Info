package com.example.julian.locationservice;

import android.Manifest;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.meedamian.info.PermChecker;
import com.meedamian.info.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeoChecker extends PermChecker implements
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private GoogleApiClient mGoogleApiClient;
    private LocationAvailabler la;
    private Context c;


    public GeoChecker(Context context, LocationAvailabler locationer) {
        la = locationer;
        c = context;

        if (locationer != null && isPermitted(c))
            init();
    }
    public GeoChecker(Context c) { this(c, null); }

    public void init() {
        buildGoogleApiClient(c).connect();
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

    @Override public void onConnectionSuspended(int i) {}
    @Override public void onConnectionFailed(ConnectionResult connectionResult) {}

    public static String getLocationQuery(@Nullable String country, @Nullable String city) {
        if (country == null)
            return city;

        if (city == null)
            return null;

        return country + ", " + city;
    }

    public LatLng getCoords(@Nullable String country, @Nullable String city) {
        String locationQuery = getLocationQuery(country, city);
        if (locationQuery == null)
            return null;

        try {
            Address address = new Geocoder(c).getFromLocationName(locationQuery, 1).get(0);
            return new LatLng(
                address.getLatitude(),
                address.getLongitude()
            );

        } catch(IOException e) {
            return null;
        }
    }

    protected synchronized GoogleApiClient buildGoogleApiClient(Context c) {
        if (mGoogleApiClient != null)
            return mGoogleApiClient;

        return mGoogleApiClient = new GoogleApiClient.Builder(c)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
    }


    @Override
    protected int getNotificationId() {
        return MISSING_LOCATION_PERM;
    }

    @Override
    protected String getPermission() {
        return PERMISSION;
    }

    @Override
    protected int getSmallIcon() {
        return android.R.drawable.ic_secure;
    }

    @Override
    protected int getText() {
        return R.string.location_permission_content_text;
    }


    public interface LocationAvailabler {
        void onLocationAvailable(String country, String city);
    }
}
