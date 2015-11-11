package com.example.julian.locationservice;

import android.content.Context;

import com.meedamian.info.BasicData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DataUploader {

    private Context c;
    private String phone;
    private String vanity;
    private String country;
    private String city;

    public DataUploader(Context context) {
        c = context;
    }


    public void upload() {
        String key = BasicData.getPrivateId(c);

        HttpURLConnection urlConnection;
        URL url;
        String http = "https://basic-data.parseapp.com/update";

        JSONObject jo = new JSONObject();
        try {
            jo.put("key", key);

            if (phone != null)
                jo.put("phone", phone);

            else if (vanity != null)
                jo.put("vanity", vanity);

            else if (country != null)
                jo.put("country", country);

            else if (city != null)
                jo.put("city", city);

        } catch (JSONException ignored) {
        } finally {
            try {
                url = new URL(http);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Accept", "application/json");

                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(String.valueOf(jo));
                writer.flush();
                writer.close();
                out.close();

                urlConnection.connect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // send `jo`
            // as POST body
            // to https://basic-data.parseapp.com/update
        }
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
