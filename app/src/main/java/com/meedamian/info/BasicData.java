package com.meedamian.info;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static com.meedamian.info.RemoteData.CITY;
import static com.meedamian.info.RemoteData.COUNTRY;
import static com.meedamian.info.RemoteData.PHONE;

public class BasicData {
    String hash;
    String city;
    String country;
    String phone;

    public BasicData(String country, String city, String phone) {
        this.country = country;
        this.city = city;
        this.phone = phone;
    }

    BasicData(JsonObject json) {
        this.country = getStringFromJson(json, COUNTRY);
        this.city = getStringFromJson(json, CITY);
        this.phone = getStringFromJson(json, PHONE);
    }

    @Nullable
    private static String getStringFromJson(@NonNull JsonObject json, @NonNull String name) {
        JsonElement el = json.get(name);
        return (el == null) ? null : el.getAsString();
    }

    @Override
    public String toString() {
        return (this.hash != null ? this.hash : "NO HASH") + " "
            + (this.country != null ? this.country : "NO COUNTRY") + " "
            + (this.city != null ? this.city : "NO CITY") + " "
            + (this.phone != null ? this.phone : "NO PHONE");
    }
}
