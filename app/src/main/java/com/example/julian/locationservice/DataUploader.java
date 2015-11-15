package com.example.julian.locationservice;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.meedamian.info.BasicData;

public class DataUploader {

    protected static final String KEY     = "key";
    public static final    String PHONE   = "phone";
    public static final    String VANITY  = "vanity";
    public static final    String COUNTRY = "country";
    public static final    String CITY    = "city";

    public static final String    API_URL = "https://basic-data.parseapp.com/";

    private String phone;
    private String vanity;
    private String country;
    private String city;

    private Context c;

    public DataUploader(Context context) {
        c = context;
    }


    public void upload() {

        JsonObject jo = new JsonObject();
        jo.addProperty(KEY, BasicData.getPrivateId(c));

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

    public DataUploader setLocation(String country, String city) {
        this.country = country;
        this.city = city;
        return this;
    }

    public DataUploader setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public DataUploader setVanity(String vanity) {
        this.vanity = vanity;
        return this;
    }
}
