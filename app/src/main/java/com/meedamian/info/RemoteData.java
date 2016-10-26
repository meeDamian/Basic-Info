package com.meedamian.info;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

class RemoteData {

    private static final String API_URL = "https://basic-data.herokuapp.com";
    private static final String BASE_GIST_URL = "https://api.github.com";

    static final String HASH    = "hash";
    static final String PHONE   = "phone";
    static final String COUNTRY = "country";
    static final String CITY    = "city";


    static void fetchFresh(@NonNull final DataCallback dc) {
        new Retrofit.Builder()
            .baseUrl(BASE_GIST_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GistApi.class)
            .get("d881897abb1b251a912d8a31c2b59298")
            .enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response == null) {
                    dc.onError(null);
                    return;
                }

                JsonElement filesRaw = response.body().get("files");
                if (filesRaw == null) {
                    dc.onError(null);
                    return;
                }

                JsonObject files = filesRaw.getAsJsonObject();
                JsonElement locRaw = files.get("location.json");
                if (locRaw == null) {
                    dc.onError(null);
                    return;
                }

                JsonObject loc = locRaw.getAsJsonObject();
                JsonElement contentString = loc.get("content");
                if (contentString == null) {
                    dc.onError(null);
                    return;
                }

                JsonElement content = new JsonParser().parse(contentString.getAsString());
                if (content == null) {
                    dc.onError(null);
                    return;
                }

                JsonObject data = content.getAsJsonObject();
                BasicData bd = new BasicData(data);

                dc.onDataReady(bd);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                dc.onError(t.toString());
            }
        });
    }

    static void upload(Context c, BasicData bd, final @Nullable SaveCallback sc) {
        bd.hash = RemoteData.getPrivateId(c);

        new Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BasicDataApi.class)
            .patch(bd)
            .enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (sc == null)
                        return;

                    if (response != null && response.body().isJsonObject()) {
                        Log.d("basic data", response.body().toString());
                        // TODO: show status of Twitter, gist and db
                        sc.onSave();
                        return;
                    }

                    String msg = response != null && response.body() != null ?
                        response.body().toString()
                        : "Unknown error";

                    sc.onError(msg);
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    if (sc == null) {
                        return;
                    }

                    sc.onError(t != null ? t.toString() : "Unknown error");
                }
            });
    }



    static boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Nullable
    private static String hash(@NonNull String s) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(s.getBytes());
            return String.format("%064x", new java.math.BigInteger(1, digest));

        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    };

    @SuppressLint("HardwareIds")
    private static String getPrivateId(@NonNull Context c) {
        String androidId = Settings.Secure.getString(
            c.getContentResolver(),
            Settings.Secure.ANDROID_ID
        );

        return hash(androidId + "->privateId");
    }

    interface GenericCallback {
        void onError(String msg);
    }

    interface DataCallback extends GenericCallback {
        void onDataReady(BasicData data);
    }

    interface SaveCallback extends GenericCallback {
        void onSave();
    }

    interface BasicDataApi {
        @PATCH("/")
        Call<JsonObject> patch(@Body BasicData patch);
    }

    interface GistApi {
        @Headers("User-Agent: meeDamian/Basic-Data")
        @GET("/gists/{id}")
        Call<JsonObject> get(@Path("id") String id);
    }
}
