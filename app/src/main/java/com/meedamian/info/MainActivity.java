package com.meedamian.info;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.example.julian.locationservice.DataUploader;
import com.example.julian.locationservice.GeoChecker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

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

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMap();

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
        DataUploader du = new DataUploader(this);

        String phoneNo = phoneET.getText().toString();
        if (phoneNo.length() > 0)
            du.setPhone(phoneNo);

        String vanityUrl = vanityET.getText().toString();
        if (vanityUrl.length() > 0)
            du.setVanity(vanityUrl);

        du.upload();

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

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng sydney = new LatLng(-33.867, 151.206);

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));

        map.addMarker(new MarkerOptions()
                .title("Sydney")
                .snippet("The most populous city in Australia.")
                .position(sydney));
    }
}