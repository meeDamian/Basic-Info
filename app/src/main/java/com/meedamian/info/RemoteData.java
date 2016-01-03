package com.meedamian.info;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RemoteData {

    private static final String API_BASE_URL   = "https://basic-data.parseapp.com/";
    private static final String API_UPLOAD_URL = API_BASE_URL + "update";

    private static final String KEY     = "key";
    public  static final String PHONE   = "phone";
    public  static final String COUNTRY = "country";
    public  static final String CITY    = "city";
    public  static final String VANITY  = "vanity";


    public static void fetchFresh(@NonNull Context c, @NonNull final DataCallback dc) {
        if (isNetworkAvailable(c)){
            Ion.with(c)
                .load(getPublicUrl(c))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                    JsonElement locRaw = result.get(LocalData.LOCATION);
                    if (locRaw == null) {
                        dc.onDataReady(null, null, null, null);
                        return;
                }

                JsonObject loc = locRaw.getAsJsonObject();
                dc.onDataReady(
                    getStringFromJson(result, VANITY),
                    getStringFromJson(result, PHONE),
                    getStringFromJson(loc,    COUNTRY),
                    getStringFromJson(loc,    CITY)
                );
                }
            });
        }
    }

    static public boolean isNetworkAvailable(Context c){
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static void upload(@NonNull Context c, @Nullable String vanity, @Nullable String phone, @Nullable String country, @Nullable String city) {
        JsonObject jo = new JsonObject();
        jo.addProperty(KEY, RemoteData.getPrivateId(c));

        if (vanity != null)
            jo.addProperty(VANITY, vanity);

        if (phone != null)
            jo.addProperty(PHONE, phone);

        if (country != null)
            jo.addProperty(COUNTRY, country);

        if (city != null)
            jo.addProperty(CITY, city);

        Ion.with(c)
            .load(API_UPLOAD_URL)
            .setJsonObjectBody(jo)
            .asString()
            .setCallback(new FutureCallback<String>() {
                @Override
                public void onCompleted(Exception e, @Nullable String result) {
                if (result != null)
                    Log.d("Basic Data", result);
                }
            });
    }

    public static String getPublicUrl(@NonNull Context c) {
        return API_BASE_URL + getPublicId(c);
    }
    public static String getPrettyUrl(@NonNull Context c, @Nullable String vanity) {
        return API_BASE_URL + (vanity != null ? vanity : getPublicId(c));
    }


    @Nullable
    private static String getStringFromJson(@NonNull JsonObject json, @NonNull String name) {
        JsonElement tmp = json.get(name);
        return (tmp == null) ? null : tmp.getAsString();
    }

    public static String getPrivateId(@NonNull Context c) {
        return Settings.Secure.getString(
            c.getContentResolver(),
            Settings.Secure.ANDROID_ID
        );
    }

    @Nullable
    public static String getPublicId(@NonNull Context c) {
        String id = getPrivateId(c);

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(id.getBytes());
            return String.format("%064x", new java.math.BigInteger(1, digest)).substring(0, 8);

        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public interface DataCallback {
        void onDataReady(
            @Nullable String vanity,
            @Nullable String phone,
            @Nullable String country,
            @Nullable String city
        );
    }
}
