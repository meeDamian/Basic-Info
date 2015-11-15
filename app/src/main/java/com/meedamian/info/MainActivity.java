package com.meedamian.info;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.example.julian.locationservice.GeoChecker;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private EditText phoneET;

    private EditText vanityET;
    private TextInputLayout vanityWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneET = (EditText) findViewById(R.id.phone);
        vanityET = (EditText) findViewById(R.id.vanity);
        vanityWrapper = (TextInputLayout) findViewById(R.id.vanityWrapper);
        vanityWrapper.setHint(String.format(
            getString(R.string.current_url),
            BasicData.getPublicId(this)
        ));


        vanityET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, final boolean hasFocus) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    vanityET.setHint(hasFocus ? "Set your vanity" : "");
                    }
                }, 200);
            }
        });

        init();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onPause() {
        BasicData bd = new BasicData(this);

        String phoneNo = phoneET.getText().toString();
        if (phoneNo.length() > 0)
            bd.setPhone(phoneNo);

        String vanityUrl = vanityET.getText().toString();
        if (vanityUrl.length() > 0)
            bd.setVanity(vanityUrl);

        bd.upload();

        super.onPause();
    }

    private void init() {
        BasicData.fetchFresh(this, new BasicData.DataCallback() {
            @Override
            public void onDataReady(String vanity, String phone, String country, String city) {

            if (vanity != null)
                vanityET.setText(vanity);

            if (phone != null)
                phoneET.setText(phone);

            }
        });

        Receiver.setAlarm(this);
        MainActivityPermissionsDispatcher.initSimWithCheck(this);
        MainActivityPermissionsDispatcher.initGeoWithCheck(this);
    }

    @NeedsPermission(SimChecker.PERMISSION)
    protected void initSim() {
        new SimChecker(this);
    }

    @NeedsPermission(GeoChecker.PERMISSION)
    protected void initGeo() {
        new GeoChecker(this);
    }
}