package com.meedamian.info;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
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

    private static final String API_URL = "https://basic-data.parseapp.com/";
    private static final String KEY     = "key";

    private static SharedPreferences getSp(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c);
    }

    private static String getUpdatedKey(String key) {
        return key + "_updated";
    }

    public static String getString(Context c, String key) {
        return getSp(c).getString(key, null);
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

    private static String getStringFromJson(JsonObject json, String name) {
        JsonElement tmp = json.get(name);
        return (tmp == null) ? null : tmp.getAsString();
    }
    public static void fetchFresh(Context c, final DataCallback dc) {
        Ion.with(c)
            .load(API_URL + getPublicId(c))
            .asJsonObject()
            .setCallback(new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                JsonObject loc = result.get(LOCATION).getAsJsonObject();
                dc.onDataReady(
                    getStringFromJson(result,   VANITY),
                    getStringFromJson(result, PHONE),
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

    public static class Uploader {
        private String phone;
        private String vanity;
        private String country;
        private String city;

        private Context c;

        public Uploader(Context context) {
            c = context;
        }

        public Uploader setLocation(String country, String city) {
            this.country = country;
            this.city = city;
            return this;
        }

        public Uploader setPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public Uploader setVanity(String vanity) {
            this.vanity = vanity;
            return this;
        }

        public void upload() {

            JsonObject jo = new JsonObject();
            jo.addProperty(KEY, getPrivateId(c));

            if (vanity != null)
                jo.addProperty(VANITY,  vanity);

            if (phone != null)
                jo.addProperty(PHONE,   phone);

            if (country != null)
                jo.addProperty(COUNTRY, country);

            if (city != null)
                jo.addProperty(CITY,    city);

            Ion.with(c)
                .load(API_URL + "update")
                .setJsonObjectBody(jo)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        Log.d("Basic Data", result);
                    }
                });
        }
    }

    public interface DataCallback {
        void onDataReady(String vanity, String phone, String country, String city);
    }
}
