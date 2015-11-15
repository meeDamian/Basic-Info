package com.meedamian.info;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.example.julian.locationservice.DataUploader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

public class BasicData {

    public static final String SUBSCRIBER_ID = "subscriber";
    public static final String PHONE_NO      = "phone";
    public static final String COUNTRY       = "country";
    public static final String CITY          = "city";
    public static final String VANITY        = "vanity";

    public static final String _LOCATION     = "location";

    private BasicData() {}

    private static SharedPreferences getSp(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c);
    }

    private static String getUpdatedKey(String key) {
        return key + "_updated";
    }

    public static String getString(Context c, String key) {
        return getSp(c).getString(key, null);
    }

    public static Set<String> getStringSet(Context c, String key) {
        return getSp(c).getStringSet(key, null);
    }

    public static Integer getInt(Context c, String key) {
        int i = getSp(c).getInt(key, -666);
        return i != -666 ? i : null;
    }

    private static SharedPreferences.Editor getSpEd(Context c) {
        return getSp(c).edit();
    }
    private static void saveSpEd(String key, SharedPreferences.Editor ed) {
        ed.putLong(getUpdatedKey(key), System.currentTimeMillis()).apply();
    }

    public static void update(Context c, String key, String val) {
        saveSpEd(key, getSpEd(c).putString(key, val));
    }
    public static void update(Context c, String key, Set<String> val) {
        saveSpEd(key, getSpEd(c).putStringSet(key, val));
    }
    public static void update(Context c, String key, Integer val) {
        saveSpEd(key, getSpEd(c).putInt(key, val));
    }

    private static String getStringFromJson(JsonObject json, String name) {
        JsonElement tmp = json.get(name);
        return (tmp == null) ? null : tmp.getAsString();
    }
    public static void fetchFresh(Context c, final DataCallback dc) {
        Ion.with(c)
            .load(DataUploader.API_URL + getPublicId(c))
            .asJsonObject()
            .setCallback(new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {

                JsonObject loc = result.get(_LOCATION).getAsJsonObject();
                dc.onDataReady(
                    getStringFromJson(result,   VANITY),
                    getStringFromJson(result,   PHONE_NO),
                    getStringFromJson(loc,      COUNTRY),
                    getStringFromJson(loc,      CITY)
                );
                }
            });

    }

    public static String getPrivateId(Context c) {
        return Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
    public static String getPublicId(Context c) {
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
        void onDataReady(String vanity, String phone, String country, String city);
    }
}
