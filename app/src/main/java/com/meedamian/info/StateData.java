package com.meedamian.info;

import android.content.Context;
import android.text.TextWatcher;
import android.view.View;

import com.example.julian.locationservice.GeoChecker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.meedamian.info.meh.SimpleTextWatcher;

import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableBoolean;

public class StateData extends BaseObservable {

    private Context c;
    private View rootView;

    StateData(Context context) {
        this.c = context;
    }
    void setRootView(View v) {
        this.rootView = v;
    }

    public final ObservableBoolean userFieldsEnabled = new ObservableBoolean();
    void enableUserFields() {
        userFieldsEnabled.set(true);
    }

    public final ObservableBoolean locationFieldsEnabled = new ObservableBoolean();
    void enableLocationFields() {
        locationFieldsEnabled.set(true);
    }

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

    void setLocation(String country, String city) {
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


    // VARIOUS
    void showSnackbar(String text, @Nullable String actionName, @Nullable View.OnClickListener actionCallback) {
        int length = actionName == null && actionCallback == null
            ? Snackbar.LENGTH_LONG
            : Snackbar.LENGTH_INDEFINITE;

        Snackbar snackbar = Snackbar.make(rootView, text, length);
        if (actionName != null && actionCallback != null)
            snackbar.setAction(actionName, actionCallback);

        snackbar.show();
    }

    private GoogleMap googleMap;
    void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
        tryToAddMarker();
    }
    private LatLng position;
    private void setPosition(LatLng latLng) {
        this.position = latLng;
        tryToAddMarker();
    }
    private void setPositionFrom(String country, String city) {
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
        LocalData.saveUserEdits(c, new BasicData(getCountry(), getCity(), getPhone()), new RemoteData.SaveCallback() {
            @Override
            public void onSave() {
                showSnackbar(c.getString(R.string.snackbar_saved), null, null);
            }

            @Override
            public void onError(String msg) {
                showSnackbar(c.getString(R.string.snackbar_save_error, msg), null, null);
            }
        });
    }
}
