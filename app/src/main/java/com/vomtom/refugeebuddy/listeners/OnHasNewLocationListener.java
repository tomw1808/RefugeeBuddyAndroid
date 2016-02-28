package com.vomtom.refugeebuddy.listeners;

import android.location.Location;

/**
 * Created by Thomas on 12.02.2016.
 */
public interface OnHasNewLocationListener {
    void locationAdded(Location location);
}
