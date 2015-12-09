package com.meedamian.info;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.julian.locationservice.GeoChecker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private EditText phoneET;
    private EditText vanityET;

    GoogleMap mGoogleMap;

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

            GeoChecker geoChecker = new GeoChecker(getApplication());
            geoChecker.locationQuery(country, city, mGoogleMap);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayShowTitleEnabled(false);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        mGoogleMap = mapFragment.getMap();
        mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

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

    // A method to find height of the status bar
    public int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return (resourceId > 0)
            ? getResources().getDimensionPixelSize(resourceId)
            : 0;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void showEditDialog(final String what) {
        final EditText input = new EditText(this);
        final String oldValue = bd.getString(what);
        if (oldValue != null)
            input.setText(oldValue);

        new AlertDialog.Builder(this)
            .setTitle(String.format("Change %s name", what))
            .setView(input, convertToDp(20), convertToDp(5), convertToDp(25), 0)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newValue = input.getText().toString().trim();
                    if (!newValue.equals(oldValue))
                        bd.setReplacer(what, oldValue, newValue);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                }
            })
            .create()
            .show();
    }

    public int convertToDp(int value){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_country: showEditDialog(BasicData.COUNTRY);  break;
            case R.id.menu_city:    showEditDialog(BasicData.CITY);     break;
        }

        return super.onOptionsItemSelected(item);
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