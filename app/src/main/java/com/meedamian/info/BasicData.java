package com.meedamian.info;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BasicData {
    public static final String PHONE   = "phone";
    public static final String COUNTRY = "country";
    public static final String CITY    = "city";
    public static final String VANITY  = "vanity";


    public static final String LOCATION      = "location";
    public static final String SUBSCRIBER_ID = "subscriber";

    private static final String API_BASE_URL = "https://basic-data.parseapp.com/";
    private static final String API_UPLOAD_URL = API_BASE_URL + "update";

    private static final String KEY = "key";

    private Context c;

    private String phone;
    private String vanity;
    private String country;
    private String city;

    private BasicData(Context context, @Nullable DataCallback dc) {
        this.c = context;

        if (dc != null)
            refreshData(dc);
    }
    private static BasicData instance = null;
    public static BasicData getInstance(Context context, @Nullable DataCallback dc) {
        if (instance == null)
            instance = new BasicData(context, dc);

        else if (dc != null)
            instance.refreshData(dc);

        return instance;
    }
    public static BasicData getInstance(Context c) {
        return getInstance(c, null);
    }

    public String getPrivateId() {
        return Settings.Secure.getString(
            c.getContentResolver(),
            Settings.Secure.ANDROID_ID
        );
    }
    public String getPublicId() {
        String id = getPrivateId();

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(id.getBytes());
            return String.format("%064x", new java.math.BigInteger(1, digest)).substring(0, 8);

        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
    public String getPublicUrl() {
        return API_BASE_URL + getPublicId();
    }
    public String getPrettyUrl() {
        return API_BASE_URL + (vanity != null ? vanity : getPublicId());
    }
    public String getString(String key) {
        return getSp().getString(key, null);
    }
    public void cacheString(String key, String val) {
        getSpEditor()
            .putString(key, val)
            .putLong(getUpdatesKey(key), System.currentTimeMillis())
            .apply();
    }

    private void cleanTheDirt() {
        vanityDirty =
        phoneDirty =
        locationDirty =
            false;
    }

    private boolean locationDirty = false;
    public BasicData setLocation(String country, String city) {
        this.country = country;
        this.city = city;
        locationDirty = true;
        return this;
    }

    private boolean phoneDirty = false;
    public BasicData setPhone(String phone) {
        this.phone = phone;
        phoneDirty = true;
        return this;
    }

    private boolean vanityDirty = false;
    public BasicData setVanity(String vanity) {
        this.vanity = vanity;
        vanityDirty = true;
        return this;
    }

    private SharedPreferences getSp() {
        return PreferenceManager.getDefaultSharedPreferences(c);
    }
    private SharedPreferences.Editor getSpEditor() {
        return getSp().edit();
    }
    private static String getUpdatesKey(String key) {
        return key + "_updated";
    }
    private static String getStringFromJson(JsonObject json, String name) {
        JsonElement tmp = json.get(name);
        return (tmp == null) ? null : tmp.getAsString();
    }

    private void refreshData(final DataCallback dc) {
        Ion.with(c)
            .load(getPublicUrl())
            .asJsonObject()
            .setCallback(new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                JsonObject loc = result.get(LOCATION).getAsJsonObject();

                vanity = getStringFromJson(result, VANITY);
                phone = getStringFromJson(result, PHONE);
                country = getStringFromJson(loc, COUNTRY);
                city = getStringFromJson(loc, CITY);

                cleanTheDirt();

                dc.onDataReady(vanity, phone, country, city);
                }
            });
    }

    public BasicData save() {
        if (locationDirty) {
            cacheString(COUNTRY, country);
            cacheString(CITY, city);
        }

        if (phoneDirty)
            cacheString(PHONE, phone);

        if (vanityDirty)
            cacheString(VANITY, vanity);

        cleanTheDirt();

        return this;
    }

    public void upload() {
        JsonObject jo = new JsonObject();
        jo.addProperty(KEY, getPrivateId());

        if (vanity != null)
            jo.addProperty(VANITY,  vanity);

        if (phone != null)
            jo.addProperty(PHONE,   phone);

        if (country != null)
            jo.addProperty(COUNTRY, country);

        if (city != null)
            jo.addProperty(CITY,    city);

        Ion.with(c)
            .load(API_UPLOAD_URL)
            .setJsonObjectBody(jo)
            .asString()
            .setCallback(new FutureCallback<String>() {
                @Override
                public void onCompleted(Exception e, String result) {
                    Log.d("Basic Data", result);
                }
            });
    }

    public interface DataCallback {
        void onDataReady(String vanity, String phone, String country, String city);
    }
}
