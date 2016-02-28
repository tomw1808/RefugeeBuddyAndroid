package com.vomtom.refugeebuddy.listeners;

import android.database.Cursor;

/**
 * Created by Thomas on 12.02.2016.
 */
public interface OnDataSetReceivedListener {
    void onDataSetReceived(Cursor cursor);
}
