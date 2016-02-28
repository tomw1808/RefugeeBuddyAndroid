package com.vomtom.refugeebuddy.listeners;

import android.location.Address;

import java.util.List;

public interface OnGeocoderFinishedListener {
    void onFinished(List<Address> results);
}