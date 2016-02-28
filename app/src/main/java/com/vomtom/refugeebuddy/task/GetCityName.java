package com.vomtom.refugeebuddy.task;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;

import com.vomtom.refugeebuddy.listeners.OnGeocoderFinishedListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Thomas on 08.02.2016.
 *
 * Thanks to http://stackoverflow.com/questions/18221614/how-i-can-get-the-city-name-of-my-current-position
 */
public class GetCityName {
    public void getCityName(final Context context, final Location location, final OnGeocoderFinishedListener listener) {
        new AsyncTask<Void, Integer, List<Address>>() {
            @Override
            protected List<Address> doInBackground(Void... arg0) {
                Geocoder coder = new Geocoder(context, Locale.ENGLISH);
                List<Address> results = null;
                try {
                    results = coder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (IOException e) {
                    // nothing
                }
                return results;
            }

            @Override
            protected void onPostExecute(List<Address> results) {
                if (results != null && listener != null) {
                    listener.onFinished(results);
                }
            }
        }.execute();
    }
}
