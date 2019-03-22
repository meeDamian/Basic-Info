package com.example.julian.locationservice;

import android.Manifest;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.meedamian.info.BasicData;
import com.meedamian.info.PermChecker;
import com.meedamian.info.R;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GeoChecker extends PermChecker implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private GoogleApiClient mGoogleApiClient;
    private LocationAvailabler la;
    private Context c;


    public GeoChecker(Context context) {
        c = context;
        buildGoogleApiClient(c).connect();
    }

    private synchronized GoogleApiClient buildGoogleApiClient(Context c) {
        if (mGoogleApiClient != null)
            return mGoogleApiClient;

        return mGoogleApiClient = new GoogleApiClient.Builder(c)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (la != null)
            getNewLocation(la);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public void getNewLocation(@Nullable LocationAvailabler localCallback) {
        isPermitted(c);

        if (!mGoogleApiClient.isConnected()) {
            this.la = localCallback;
            return;
        }


        if (localCallback != null) {
            Address a = getAddress(c, LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));

            localCallback.onLocationAvailable(new BasicData(a.getCountryName(), a.getLocality(), null));
        }
    }

    @Contract(value = "!null, null -> null", pure = true)
    public static String getLocationQuery(@Nullable String country, @Nullable String city) {
        if (country == null)
            return city;

        if (city == null)
            return null;

        return country + ", " + city;
    }


    @Contract("_, null -> null")
    private static Address getAddress(@NonNull Context c, @Nullable Location location) {
        if (location == null)
            return null;

        try {
            List<Address> addresses = new Geocoder(c, Locale.getDefault()).getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1
            );

            return addresses.get(0);

        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static LatLng getCoords(@NonNull Context c, @Nullable String country, @Nullable String city) {
        String locationQuery = getLocationQuery(country, city);
        if (locationQuery == null)
            return null;

        try {
            Address address = new Geocoder(c).getFromLocationName(locationQuery, 1).get(0);
            return new LatLng(
                    address.getLatitude(),
                    address.getLongitude()
            );

        } catch (IOException e) {
            return null;
        }
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
        void onLocationAvailable(@NonNull BasicData bd);
    }
}
