package com.meedamian.info;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

public class BasicData {

    public static final String SUBSCRIBER_ID = "subscriber";
    public static final String PHONE_NO      = "phone";
    public static final String LOCATION      = "location";
    public static final String VANITY        = "vanity";

    private BasicData() {}

    private static SharedPreferences getSp(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c);
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

    public static void update(Context c, String key, String val) {
        getSp(c)
            .edit()
            .putString(key, val)
            .putLong(key + "_updated", System.currentTimeMillis())
            .apply();
    }
    public static void update(Context c, String key, Set<String> val) {
        getSp(c)
            .edit()
            .putStringSet(key, val)
            .putLong(key + "_updated", System.currentTimeMillis())
            .apply();
    }
    public static void update(Context c, String key, Integer val) {
        getSp(c)
            .edit()
            .putInt(key, val)
            .putLong(key + "_updated", System.currentTimeMillis())
            .apply();
    }


    protected static String getPrivateId(Context c) {
        return Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
    public String getPublicId(Context c) {
        String id = getPrivateId(c);

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(id.getBytes());
            return new String(messageDigest.digest());

        } catch (NoSuchAlgorithmException e) {
            return null;
        }

    }
}
