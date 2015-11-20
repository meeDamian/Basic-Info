package com.meedamian.info;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    private static final String API_BASE_URL   = "https://basic-data.parseapp.com/";
    private static final String API_UPLOAD_URL = API_BASE_URL + "update";

    private static final String KEY = "key";
    private static final String KEY_UPDATED_SUFFIX  = "_updated";
    private static final String KEY_REPLACER_SUFFIX = "_replacer";

    private Context c;

    private String phone;
    private String vanity;
    private String country;
    private String city;


    // Lazy singleton stuff
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


    // credentials
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


    // URLs
    public String getPublicUrl() {
        return API_BASE_URL + getPublicId();
    }
    public String getPrettyUrl() {
        return API_BASE_URL + (vanity != null ? vanity : getPublicId());
    }


    // Shared Preferences stuff
    private SharedPreferences getSp() {
        return PreferenceManager.getDefaultSharedPreferences(c);
    }
    private SharedPreferences.Editor getSpEditor() {
        return getSp().edit();
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


    // Replacing
    private static String getReplacerKey(String key) {
        return key + KEY_REPLACER_SUFFIX;
    }
    public void setReplacer(String what, String from, String to) {
        if (!what.equals(CITY) && !what.equals(COUNTRY)) {
            Log.w("Basic Data", "Attempt to set illegal 'replacer' blocked");
            return;
        }

        Replacer oldReplacer = getReplacer(what);
        Replacer newReplacer = new Replacer(from, to);

        // Fix `from` if another `Replacer` was already set
        if (oldReplacer.exists() && oldReplacer.to.equals(from))
            newReplacer.from = oldReplacer.from;

        saveReplacer(what, newReplacer);

        if (what.equals(CITY))
            this.city = to;

        if (what.equals(COUNTRY))
            this.country = to;
    }
    private Replacer getReplacer(String what) {
        return new Replacer(
            getSp().getString(getReplacerKey(what), null)
        );
    }
    private void saveReplacer(String what, Replacer r) {
        getSpEditor()
            .putString(getReplacerKey(what), r.toJsonString())
            .apply();
    }
    private String checkReplace(String what, String from) {
        Replacer r = getReplacer(what);
        if (r.exists() && from.equals(r.from))
            return r.to;

        return from;
    }

    // State-creators for `.upload()` method
    private void cleanTheDirt() {
        vanityDirty =
        phoneDirty =
        locationDirty =
            false;
    }

    private boolean locationDirty = false;
    public BasicData setLocation(String country, String city) {

        if (country != null) {
            country = checkReplace(COUNTRY, country.trim());
            if (country != null)
                this.country = country;
        }

        if (city != null) {
            city = checkReplace(CITY, city.trim());
            if (city != null)
                this.city = city;
        }

        locationDirty = true;
        return this;
    }

    private boolean phoneDirty = false;
    public BasicData setPhone(String phone) {
        this.phone = phone.trim();
        phoneDirty = true;
        return this;
    }

    private boolean vanityDirty = false;
    public BasicData setVanity(String vanity) {
        this.vanity = vanity.trim();
        vanityDirty = true;
        return this;
    }


    // Magical helpers
    private static String getUpdatesKey(String key) {
        return key + KEY_UPDATED_SUFFIX;
    }
    private static String getStringFromJson(JsonObject json, String name) {
        JsonElement tmp = json.get(name);
        return (tmp == null) ? null : tmp.getAsString();
    }


    // Network stuff
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

    // Local stuff
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


    public interface DataCallback {
        void onDataReady(
            @Nullable String vanity,
            @Nullable String phone,
            @Nullable String country,
            @Nullable String city
        );
    }


    // because inner classes are cool
    private class Replacer {
        private static final String FROM   = "from";
        private static final String TO     = "to";

        public String from;
        public String to;

        public Replacer(@Nullable String jsonString) {
            if (jsonString != null) {
                JsonObject json = new JsonParser()
                    .parse(jsonString)
                    .getAsJsonObject();

                from = getStringFromJson(json, Replacer.FROM);
                to = getStringFromJson(json, Replacer.TO);
            }
        }
        public Replacer(@NonNull String from, @NonNull String to) {
            this.from = from.trim();
            this.to = to.trim();
        }

        public String toJsonString() {
            if (!exists())
                return null;

            JsonObject jo = new JsonObject();
            jo.addProperty(Replacer.FROM, from);
            jo.addProperty(Replacer.TO, to);

            return jo.toString();
        }

        private boolean exists() {
            return from != null && to != null;
        }
    }
}
