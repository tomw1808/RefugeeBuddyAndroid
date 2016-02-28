package com.vomtom.refugeebuddy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;
import com.vomtom.refugeebuddy.activities.ListLocationsActivity;
import com.vomtom.refugeebuddy.activities.NewsActivity;
import com.vomtom.refugeebuddy.listeners.OnGeocoderFinishedListener;
import com.vomtom.refugeebuddy.listeners.OnHasNewLocationListener;
import com.vomtom.refugeebuddy.services.LocationService;
import com.vomtom.refugeebuddy.task.GetCityName;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnHasNewLocationListener {

    LocationService mService;
    boolean mBound = false;
    private boolean isFabOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(startLocationServiceClickListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, ListLocationsActivity.class);
            startActivity(intent);
            return true;
        }


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_news) {
            Intent intent = new Intent(MainActivity.this, NewsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        bindToService();
        super.onResume();
    }

    @Override
    protected void onPause() {
        // Unregister the broadcast receiver that was registered during onResume().
        if (mBound) {
            mService.unsetOnHasNewLocationListener();
            try {
                unbindService(mConnection);
                mBound = false;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        super.onPause();
    }




    private void bindToService() {

        if (!mBound) {
            bindService(new Intent(this, LocationService.class), mConnection, Context.BIND_AUTO_CREATE);
        }

    }


    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.setOnHasNewLocationListener(MainActivity.this);
            if (mService.getmLocations().size() > 0) {
                updateCityName(mService.getmLocations().get(0));
                updateStats(mService.getmLocations().get(0));
            }
            if (mService.isServiceStarted()) {
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                fab.setImageResource(android.R.drawable.ic_media_pause);
            }
        }


        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            mService = null;
        }
    };


    private View.OnClickListener startLocationServiceClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            if (mBound && mService.isServiceStarted()) {
                mService.stopService();
                fab.setImageResource(android.R.drawable.ic_media_play);
            } else {
                mService.setShowTimer(false);
                mService.setLocationUpdatePriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                mService.setUpdateInterval(5 * 60 * 1000); //update every 5 minutes
                mService.setUpdateInterval(5 * 1000); //update every 5 seconds
                startService(new Intent(MainActivity.this, LocationService.class));
                fab.setImageResource(android.R.drawable.ic_media_pause);
            }
            //animateFAB(v);
        }
    };

    @Override
    public void locationAdded(Location location) {
        updateCityName(location);
        updateStats(location);
    }


    private void updateCityName(final Location location) {
        new GetCityName().getCityName(this, location, new OnGeocoderFinishedListener() {
            @Override
            public void onFinished(List<Address> results) {
                if (results.size() > 0) {
                    StringBuilder builder = new StringBuilder();
                    int maxLines = results.get(0).getMaxAddressLineIndex();
                    for (int i = 0; i < maxLines; i++) {
                        String addressStr = results.get(0).getAddressLine(i);
                        builder.append(addressStr);
                        builder.append(", ");
                    }
                    builder.append(results.get(0).getCountryName());

                    String currentAddress = builder.toString(); //This is the complete address.
                    TextView mAddressLine = (TextView) findViewById(R.id.textview_address);
                    mAddressLine.setText(currentAddress);
                }
            }
        });
    }


    private void updateStats(final Location lastLocation) {
        if (mBound) {

            TextView mLastPosition = (TextView) findViewById(R.id.textview_position);
            mLastPosition.setText(getString(R.string.lat_lng_positon, lastLocation.getLatitude(), lastLocation.getLongitude()));

            TextView mSpeedAltitude = (TextView) findViewById(R.id.textview_speed_altitude);
            mSpeedAltitude.setText(getString(R.string.speed_altitude, lastLocation.getSpeed(), (int) lastLocation.getAltitude()));

            TextView mBearingAccuracy = (TextView) findViewById(R.id.textview_bearing_accuracy);
            mBearingAccuracy.setText(getString(R.string.bearing_accuracy, lastLocation.getBearing(), (int) lastLocation.getAccuracy()));

            Button mShareLocationBtn = (Button) findViewById(R.id.btnShareLocation);
            mShareLocationBtn.setVisibility(View.VISIBLE);
            Button mOpenMapBtn = (Button) findViewById(R.id.btnOpenMap);
            mOpenMapBtn.setVisibility(View.VISIBLE);

            mOpenMapBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri    gmmIntentUri = Uri.parse("geo:" + lastLocation.getLatitude() + "," + lastLocation.getLongitude());
                    Intent mapIntent    = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                }
            });
            mShareLocationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareCurrentLocation(lastLocation);
                }
            });

            Button mTrackMe = (Button) findViewById(R.id.textview_sharelink);
            if(mService.getServerId() != null) {
                mTrackMe.setText(getString(R.string.track_me));

                mTrackMe.setVisibility(View.VISIBLE);
                mTrackMe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       shareFollowMeLink(mService.getServerId());
                    }
                });
            } else {
                mTrackMe.setVisibility(View.INVISIBLE);
            }

        }
    }

    private void shareCurrentLocation(Location location) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(Locale.ENGLISH, getString(R.string.share_location_url),location.getLatitude(),location.getLongitude())); //we do not want localization here
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_current_location)));
    }

    private void shareFollowMeLink(String server_id) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(Locale.ENGLISH, getString(R.string.track_me_link),server_id)); //we do not want localization here
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_current_location)));

    }


}
