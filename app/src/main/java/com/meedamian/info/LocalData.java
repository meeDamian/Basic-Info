package com.meedamian.info;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.example.julian.locationservice.GeoChecker;

import org.jetbrains.annotations.Contract;

class LocalData implements GeoChecker.LocationAvailabler {

    static final String SUBSCRIBER_ID = "subscriber";

    private static final String KEY_UPDATED_SUFFIX  = "_updated";

    private StateData sd;
    private GeoChecker gc;
    private Context c;

    LocalData(@NonNull Context context, StateData stateData, GeoChecker geoChecker) {
        this.c = context;
        this.sd = stateData;
        this.gc = geoChecker;

        // pre-populate interface from local storage
        sd.setPhone(getPhone());
        sd.setCountry(getCountry());
        sd.setCity(getCity());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            sd.enableLocationFields();
            sd.enableUserFields();
            }
        }, 4000);

        if(RemoteData.isNetworkAvailable(c))
            refreshData();
        else
            retrySnackbar();
    }

    public String getPhone() {
        return getString(c, RemoteData.PHONE);
    }
    private void putPhone(@Nullable String phone) {
        putPhone(c, phone);
    }
    private static void putPhone(@NonNull Context c, @Nullable String phone) {
        if (phone != null)
            cacheString(c, RemoteData.PHONE, phone);
    }

    public String getCountry() {
        return getString(c, RemoteData.COUNTRY);
    }
    private void putCountry(@Nullable String country) {
        putCountry(c, country);
    }
    private static void putCountry(@NonNull Context c, @Nullable String country) {
        if (country != null)
            cacheString(c, RemoteData.COUNTRY, country);
    }

    public String getCity() {
        return getString(c, RemoteData.CITY);
    }
    private void putCity(String city) {
        putCity(c, city);
    }
    private static void putCity(@NonNull Context c, @Nullable String city) {
        if(city != null)
            cacheString(c, RemoteData.CITY, city);
    }

    void save() {
        saveUserEdits(c, new BasicData(sd.getCountry(), sd.getCity(), sd.getPhone()), null);
    }

    static void saveUserEdits(@NonNull Context c,
                              @NonNull BasicData bd,
                              @Nullable RemoteData.SaveCallback sc) {

        // TODO: cache values only on success
        RemoteData.upload(c, bd, sc);
    }

    private void refreshData() {
        RemoteData.fetchFresh(new RemoteData.DataCallback() {

            @Override
            public void onDataReady(BasicData bd) {
                putPhone(bd.phone);
                sd.setPhone(bd.phone);
                sd.enableUserFields();

                // TODO: should those even be set here?
                putCountry(bd.country);
                sd.setCountry(bd.country);

                putCity(bd.city);
                sd.setCity(bd.city);
            }

            @Override
            public void onError(String ignored) {
                retrySnackbar();
            }
        });
    }
    private void retrySnackbar() {
        sd.showSnackbar(
            c.getString(R.string.snackbar_nointernet_text),
            c.getString(R.string.snackbar_nointernet_action), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshData();
                }
            });
    }


    void refreshLocation() {
        gc.getNewLocation(this);
    }
    static void saveLocation(@NonNull Context c, @NonNull String country, @NonNull String city) {
        RemoteData.upload(c, new BasicData(country, city, null), null);
    }
    @Override
    public void onLocationAvailable(@NonNull BasicData bd) {
        if (bd.country != null && bd.city != null)
            sd.setLocation(bd.country, bd.city);
    }

    // Shared Preferences stuff
    private static SharedPreferences getSp(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c);
    }
    private static SharedPreferences.Editor getSpEditor(Context c) {
        return getSp(c).edit();
    }
    static String getString(Context c, @NonNull String key) {
        return getSp(c).getString(key, null);
    }
    static void cacheString(Context c, @NonNull String key, @NonNull String val) {
        getSpEditor(c)
            .putString(key, val)
            .putLong(getUpdatesKey(key), System.currentTimeMillis())
            .apply();
    }

    @Contract(pure = true)
    private static String getUpdatesKey(@NonNull String key) {
        return key + KEY_UPDATED_SUFFIX;
    }
}
