package com.meedamian.info;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.julian.locationservice.GeoChecker;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private EditText phoneET;
    private EditText vanityET;

    private BasicData bd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bd = BasicData.getInstance(this, new BasicData.DataCallback() {
            @Override
            public void onDataReady(String vanity, String phone, String country, String city) {
            if (vanity != null)
                vanityET.setText(vanity);

            if (phone != null)
                phoneET.setText(phone);
            }
        });

        phoneET = (EditText) findViewById(R.id.phone);

        TextInputLayout vanityWrapper = (TextInputLayout) findViewById(R.id.vanityWrapper);
        vanityWrapper.setHint(String.format(
            getString(R.string.current_url),
            bd.getPublicId()
        ));

        vanityET = (EditText) findViewById(R.id.vanity);
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

        ImageButton copy = (ImageButton) findViewById(R.id.copy);
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData cd = ClipData.newPlainText("Basic Data user URL", bd.getPrettyUrl());
            cm.setPrimaryClip(cd);
            Toast.makeText(MainActivity.this, "URL copied to clipboard", Toast.LENGTH_LONG).show();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.save);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void init() {
        Receiver.setAlarm(this);
        MainActivityPermissionsDispatcher.initSimWithCheck(this);
        MainActivityPermissionsDispatcher.initGeoWithCheck(this);
    }

    private void save() {
        String phoneNo = phoneET.getText().toString();
        if (phoneNo.length() > 0)
            bd.setPhone(phoneNo);

        String vanityUrl = vanityET.getText().toString();
        if (vanityUrl.length() > 0)
            bd.setVanity(vanityUrl);

        bd.save().upload();
    }

    @Override
    protected void onPause() {
        save();
        super.onPause();
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