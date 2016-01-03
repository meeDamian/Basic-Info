package com.meedamian.info;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.julian.locationservice.GeoChecker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.meedamian.info.meh.SimpleTextWatcher;

public class StateData extends BaseObservable {

    private Context c;

    public StateData(Context context) {
        this.c = context;
    }

    public final ObservableBoolean userFieldsEnabled = new ObservableBoolean();
    public void enableUserFields() {
        userFieldsEnabled.set(true);
    }

    public final ObservableBoolean locationFieldsEnabled = new ObservableBoolean();
    public void enableLocationFields() {
        locationFieldsEnabled.set(true);
    }

    public final ObservableField<String> vanityHint = new ObservableField<>();
    public final ObservableField<String> prettyUrl = new ObservableField<>();

    // (Two-way) Data-Binding of COUNTRY
    private String country;
    @Bindable
    public String getCountry() {
        return country;
    }
    private void setCountryAtomic(String country) {
        this.country = country;
    }
    public void setCountry(String country) {
        setCountryAtomic(country);
        notifyPropertyChanged(BR.country);
    }
    public TextWatcher onCountryChanged = new SimpleTextWatcher() {
        @Override
        public void onTextChanged(String newCountry) {
        setCountryAtomic(newCountry);
        }
    };
    public void onCountryFocusChange(View v, boolean hasFocus) {
        if (!hasFocus)
            setPositionFrom(getCountry(), getCity());
    }


    // (Two-way) Data-Binding of CITY
    private String city;
    @Bindable
    public String getCity() {
        return city;
    }
    private void setCityAtomic(String city) {
        this.city = city;
    }
    public void setCity(String city) {
        setCityAtomic(city);
        notifyPropertyChanged(BR.city);
    }
    public TextWatcher onCityChanged = new SimpleTextWatcher() {
        @Override
        public void onTextChanged(String newCity) {
        setCityAtomic(newCity);
        }
    };
    public void onCityFocusChange(View v, boolean hasFocus) {
        if (!hasFocus)
            setPositionFrom(getCountry(), getCity());
    }

    public void setLocation(String country, String city) {
        setCountry(country);
        setCity(city);
        setPositionFrom(country, city);
        enableLocationFields();
    }


    // (Two-way) Data-Binding of PHONE
    private String phone;
    @Bindable
    public String getPhone() {
        return phone;
    }
    private void setPhoneAtomic(String phone) {
        this.phone = phone;
    }
    public void setPhone(String phone) {
        setPhoneAtomic(phone);
        notifyPropertyChanged(BR.phone);
    }
    public TextWatcher onPhoneChanged = new SimpleTextWatcher() {
        @Override
        public void onTextChanged(String newPhone) {
        setPhoneAtomic(newPhone);
        }
    };


    // (Two-way) Data-Binding of VANITY
    private String vanity;
    @Bindable
    public String getVanity() {
        return vanity;
    }
    private void setVanityAtomic(String vanity) {
        this.vanity = vanity;
    }
    public void setVanity(String vanity) {
        setVanityAtomic(vanity);
        notifyPropertyChanged(BR.vanity);
    }
    public TextWatcher onVanityChanged = new SimpleTextWatcher() {
            @Override
            public void onTextChanged(String newVanity) {
        setVanityAtomic(newVanity);
        }
    };
    public void onVanityFocusChange(final View v, final boolean hasFocus) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            ((EditText)v).setHint(hasFocus ? "Set your vanity" : "");
            }
        }, 200);
    }
    public void onCopyVanity(View v) {
        ClipboardManager cm = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData cd = ClipData.newPlainText("Basic Data user URL", prettyUrl.get());
        cm.setPrimaryClip(cd);
        Toast.makeText(c, "URL copied to clipboard", Toast.LENGTH_LONG).show();
    }


    private GoogleMap googleMap;
    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
        tryToAddMarker();
    }
    private LatLng position;
    public void setPosition(LatLng latLng) {
        this.position = latLng;
        tryToAddMarker();
    }
    public void setPositionFrom(String country, String city) {
        setPosition(GeoChecker.getCoords(c, country, city));
    }
    private void tryToAddMarker() {
        if (googleMap != null && position != null) {
            googleMap.clear();

            googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(GeoChecker.getLocationQuery(getCountry(), getCity())));

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 11));
        }
    }

    public void save(@Nullable View v) {
        LocalData.saveUserEdits(c, getVanity(), getPhone(), getCountry(), getCity());
    }
}
