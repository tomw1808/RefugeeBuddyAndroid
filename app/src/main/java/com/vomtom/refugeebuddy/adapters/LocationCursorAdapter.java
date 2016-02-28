package com.vomtom.refugeebuddy.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.vomtom.refugeebuddy.R;
import com.vomtom.refugeebuddy.contracts.LocationContract;

/**
 * Created by Thomas on 06.02.2016.
 */
public class LocationCursorAdapter extends CursorAdapter {

    public LocationCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(
                R.layout.detected_activity, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find the UI widgets.
        TextView lat = (TextView) view.findViewById(R.id.latitude);
        TextView lng = (TextView) view.findViewById(R.id.longitude);

        // Populate widgets with values.
        lat.setText("Lat: " + cursor.getString(cursor.getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_NAME_LAT)));
        lng.setText("Lng: " + cursor.getString(cursor.getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_NAME_LNG)));
    }
}
