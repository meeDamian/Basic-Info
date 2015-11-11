package com.example.julian.locationservice;

import android.content.Context;

import com.meedamian.info.BasicData;

import org.json.JSONException;
import org.json.JSONObject;

public class DataUploader {

    private Context c;
    private String phone;
    private String vanity;
    private String county;
    private String city;

    public DataUploader(Context context) {
        c = context;
    }


    public void upload() {
        String key = BasicData.getPrivateId(c);

        JSONObject jo = new JSONObject();
        try {
            jo.put("key", key);

            if (phone != null)
                jo.put("phone", phone);

            // TODO: ifs

        } catch (JSONException ignored) {}
        finally {

            // send `jo`
            // as POST body
            // to https://basic-data.parseapp.com/update
        }
    }

    public DataUploader setLocation(String country, String city) {
        this.county = country;
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
