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

public class LocalData implements GeoChecker.LocationAvailabler {

    public static final String LOCATION      = "location";
    public static final String SUBSCRIBER_ID = "subscriber";

    private static final String KEY_UPDATED_SUFFIX  = "_updated";
//    private static final String KEY_REPLACER_SUFFIX = "_replacer";

    private StateData sd;
    private GeoChecker gc;
    private Context c;

    public LocalData(@NonNull Context context, StateData stateData, GeoChecker geoChecker) {
        this.c = context;
        this.sd = stateData;
        this.gc = geoChecker;

        // pre-populate interface from local storage
        sd.setPhone(getPhone());
        sd.setVanity(getVanity());
        sd.setCountry(getCountry());
        sd.setCity(getCity());

        sd.vanityHint.set(RemoteData.getPublicId(context));
        sd.prettyUrl.set(RemoteData.getPrettyUrl(c, getVanity()));

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

    public String getVanity() {
        return getString(c, RemoteData.VANITY);
    }
    public void putVanity(@Nullable String vanity) {
        putVanity(c, vanity);
    }
    public static void putVanity(@NonNull Context c, @Nullable String vanity) {
        if (vanity != null)
            cacheString(c, RemoteData.VANITY, vanity);
    }

    public String getPhone() {
        return getString(c, RemoteData.PHONE);
    }
    public void putPhone(@Nullable String phone) {
        putPhone(c, phone);
    }
    public static void putPhone(@NonNull Context c, @Nullable String phone) {
        if (phone != null)
            cacheString(c, RemoteData.PHONE, phone);
    }

    public String getCountry() {
        return getString(c, RemoteData.COUNTRY);
    }
    public void putCountry(@Nullable String country) {
        putCountry(c, country);
    }
    public static void putCountry(@NonNull Context c, @Nullable String country) {
        if (country != null)
            cacheString(c, RemoteData.COUNTRY, country);
    }

    public String getCity() {
        return getString(c, RemoteData.CITY);
    }
    public void putCity(String city) {
        putCity(c, city);
    }
    public static void putCity(@NonNull Context c, @Nullable String city) {
        if(city != null)
            cacheString(c, RemoteData.CITY, city);
    }



    public void save() {
        saveUserEdits(c,
            sd.getVanity(),
            sd.getPhone(),
            sd.getCountry(),
            sd.getCity()
        );
    }
    public static void saveUserEdits(@NonNull Context c, @Nullable String vanity, @Nullable String phone, @Nullable String country, @Nullable String city) {
        putVanity(c, vanity);
        putPhone(c, phone);


        // TODO: check if replace happened
        putCountry(c, country);


        // TODO: check if replace happened
        putCity(c, city);


        // TODO: cache values only on success
        RemoteData.upload(c, vanity, phone, country, city);
    }

    public void refreshData() {
        RemoteData.fetchFresh(c, new RemoteData.DataCallback() {
            @Override
            public void onDataReady(@Nullable String vanity, @Nullable String phone, @Nullable String country, @Nullable String city) {
                putVanity(vanity);
                sd.setVanity(vanity);
                sd.prettyUrl.set(RemoteData.getPrettyUrl(c, vanity));

                putPhone(phone);
                sd.setPhone(phone);
                sd.enableUserFields();


                // TODO: should those even be set here?
                putCountry(country);
                sd.setCountry(country);

                putCity(city);
                sd.setCity(city);
            }

            @Override
            public void onError() {
                retrySnackbar();
            }
        });
    }
    private void retrySnackbar() {
        sd.showSnackbar(R.string.snackbar_nointernet_text, R.string.snackbar_nointernet_action, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            refreshData();
            }
        });
    }


    public void refreshLocation() {
        gc.getNewLocation(this);
    }
    public static void saveLocation(@NonNull Context c, @NonNull String country, @NonNull String city) {
        putCountry(c, country);
        putCity(c, city);
        RemoteData.upload(c, null, null, country, city);
    }
    @Override
    public void onLocationAvailable(@Nullable String country, @Nullable String city) {
        if (country != null && city != null)
            sd.setLocation(country, city);
    }



    // Shared Preferences stuff
    private static SharedPreferences getSp(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c);
    }
    private static SharedPreferences.Editor getSpEditor(Context c) {
        return getSp(c).edit();
    }
    public static String getString(Context c, @NonNull String key) {
        return getSp(c).getString(key, null);
    }
    public static void cacheString(Context c, @NonNull String key, @NonNull String val) {
        getSpEditor(c)
            .putString(key, val)
            .putLong(getUpdatesKey(key), System.currentTimeMillis())
            .apply();
    }



    @Contract(pure = true)
    private static String getUpdatesKey(@NonNull String key) {
        return key + KEY_UPDATED_SUFFIX;
    }


    // Replacing
//    private static String getReplacerKey(String key) {
//        return key + KEY_REPLACER_SUFFIX;
//    }
//    public void setReplacer(String what, String from, String to) {
//        if (!what.equals(CITY) && !what.equals(COUNTRY)) {
//            Log.w("Basic Data", "Attempt to set illegal 'replacer' blocked");
//            return;
//        }
//
//        Replacer oldReplacer = getReplacer(what);
//        Replacer newReplacer = new Replacer(from, to);
//
//        // Fix `from` if another `Replacer` was already set
//        if (oldReplacer.exists() && oldReplacer.to.equals(from))
//            newReplacer.from = oldReplacer.from;
//
//        saveReplacer(what, newReplacer);
//
//        if (what.equals(CITY))
//            this.city = to;
//
//        if (what.equals(COUNTRY))
//            this.country = to;
//    }
//    private Replacer getReplacer(String what) {
//        return new Replacer(
//            getSp().getString(getReplacerKey(what), null)
//        );
//    }
//    private void saveReplacer(String what, Replacer r) {
//        getSpEditor()
//            .putString(getReplacerKey(what), r.toJsonString())
//            .apply();
//    }
//    private String checkReplace(String what, String from) {
//        Replacer r = getReplacer(what);
//        if (r.exists() && from.equals(r.from))
//            return r.to;
//
//        return from;
//    }

//    @Nullable
//    private static String getStringFromJson(@NonNull JsonObject json, @NonNull String name) {
//        JsonElement tmp = json.get(name);
//        return (tmp == null) ? null : tmp.getAsString();
//    }


    // because inner classes are cool
//    private class Replacer {
//        private static final String FROM = "from";
//        private static final String TO   = "to";
//
//        public String from;
//        public String to;
//
//        public Replacer(@Nullable String jsonString) {
//            if (jsonString != null) {
//                JsonObject json = new JsonParser()
//                    .parse(jsonString)
//                    .getAsJsonObject();
//
//                from = getStringFromJson(json, Replacer.FROM);
//                to = getStringFromJson(json, Replacer.TO);
//            }
//        }
//        public Replacer(@NonNull String from, @NonNull String to) {
//            this.from = from.trim();
//            this.to = to.trim();
//        }
//
//        public String toJsonString() {
//            if (!exists())
//                return null;
//
//            JsonObject jo = new JsonObject();
//            jo.addProperty(Replacer.FROM, from);
//            jo.addProperty(Replacer.TO, to);
//
//            return jo.toString();
//        }
//
//        private boolean exists() {
//            return from != null && to != null;
//        }
//    }
}
