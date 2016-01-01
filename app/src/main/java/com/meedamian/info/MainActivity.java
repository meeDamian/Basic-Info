package com.meedamian.info;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.julian.locationservice.GeoChecker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.meedamian.info.databinding.ActivityMainBinding;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private StateData sd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bootstrap Data-Binding
        ActivityMainBinding amb = DataBindingUtil.setContentView(this, R.layout.activity_main);
        sd = new StateData(this, new LocalData(this), new GeoChecker(this));
        amb.setState(sd);


        // Config Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayShowTitleEnabled(false);


        // Setup Google Map Fragment
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
            googleMap.getUiSettings().setAllGesturesEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(true);

            sd.setGoogleMap(googleMap);
            }
        });


        MainActivityPermissionsDispatcher.initSimWithCheck(this);
        MainActivityPermissionsDispatcher.initGeoWithCheck(this);

        Receiver.setAlarm(this);
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

    @Override
    protected void onPause() {
        sd.save(null);
        super.onPause();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.settings:
//                Toast.makeText(this, "TODO: Open Settings...", Toast.LENGTH_LONG).show();
//                break;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

//    private void showEditDialog(final String what) {
//        final EditText input = new EditText(this);
//        final String oldValue = bd.getString(what);
//        if (oldValue != null)
//            input.setText(oldValue);
//
//        new AlertDialog.Builder(this)
//            .setTitle(String.format("Change %s name", what))
//            .setView(input, px2dp(20), px2dp(5), px2dp(25), 0)
//            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                String newValue = input.getText().toString().trim();
//                if (!newValue.equals(oldValue))
//                    bd.setReplacer(what, oldValue, newValue);
//                }
//            })
//            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//                }
//            })
//            .create()
//            .show();
//    }

//    public int px2dp(int value){
//        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
//    }



    @NeedsPermission(SimChecker.PERMISSION)
    protected void initSim() {
        new SimChecker(this);
    }

    @NeedsPermission(GeoChecker.PERMISSION)
    protected void initGeo() {
        sd.initGeo();
    }
}