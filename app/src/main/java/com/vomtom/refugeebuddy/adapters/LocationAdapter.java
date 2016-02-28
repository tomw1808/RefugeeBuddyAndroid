package com.vomtom.refugeebuddy.adapters;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vomtom.refugeebuddy.R;

import java.util.List;

/**
 * Created by Thomas on 06.02.2016.
 */
public class LocationAdapter extends ArrayAdapter<Location> {

    public LocationAdapter(Context context, int resource, List<Location> objects) {
        super(context, resource, objects);
    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Location location = getItem(position);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(
                    R.layout.detected_activity, parent, false);
        }

        // Find the UI widgets.
        TextView lat = (TextView) view.findViewById(R.id.latitude);
        TextView lng = (TextView) view.findViewById(R.id.longitude);

        // Populate widgets with values.
        lat.setText("Lat: "+location.getLatitude());
        lng.setText("Lng: "+location.getLongitude());
        return view;
    }
}
