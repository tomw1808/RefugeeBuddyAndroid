package com.vomtom.refugeebuddy.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.vomtom.refugeebuddy.contracts.LocationContract;
import com.vomtom.refugeebuddy.helper.DataConnection;
import com.vomtom.refugeebuddy.listeners.OnDataSetInsertedListener;
import com.vomtom.refugeebuddy.listeners.OnDataSetReceivedListener;
import com.vomtom.refugeebuddy.listeners.OnGeocoderFinishedListener;
import com.vomtom.refugeebuddy.listeners.OnHasNewLocationListener;
import com.vomtom.refugeebuddy.notifications.LocationServiceRunningNotification;
import com.vomtom.refugeebuddy.task.GetCityName;
import com.vomtom.refugeebuddy.task.LocationTasks;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocationService extends Service implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	public static final int notification_id = 1;
	public static final int min_accuracy    = 50;

	protected GoogleApiClient mGoogleApiClient;
	private final IBinder             mBinder        = new LocalBinder();
	private       ArrayList<Location> mLocations     = new ArrayList<>();
	private       boolean             serviceStarted = false;
	private       double              distance       = 0;
	private       String              currentAddress = "";

	private int locationUpdatePriority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

	private long    startTime      = 0;
	private boolean showTimer      = true;
	private int     updateInterval = 30 * 60 * 1000; // Update every half hour
	private String  server_id      = null;
	OnHasNewLocationListener onHasNewLocationListener;

	public void setOnHasNewLocationListener(OnHasNewLocationListener onHasNewLocationListener) {
		this.onHasNewLocationListener = onHasNewLocationListener;
	}

	public ArrayList<Location> getmLocations() {
		return this.mLocations;
	}

	public void setLocationUpdatePriority(int locationUpdatePriority) {
		this.locationUpdatePriority = locationUpdatePriority;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	public void setShowTimer(boolean showTimer) {
		this.showTimer = showTimer;
	}

	@Override
	public void onConnected(Bundle bundle) {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Toast.makeText(this, "No Permission to Access Location, Aborting", Toast.LENGTH_LONG).show();
			stopSelf();
			return;
		}
		// If we get killed, after returning from here, restart
		LocationRequest mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(updateInterval);
		mLocationRequest.setFastestInterval(30*1000); //maximum every 30 seconds.
		mLocationRequest.setPriority(locationUpdatePriority);
		//mLocationRequest.setSmallestDisplacement(250); //we don't do this, since people eventually are on hold somewhere
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, mLocationRequest, this);


		Toast.makeText(this, "Location API connection successful", Toast.LENGTH_SHORT).show();


	}

	@Override
	public void onConnectionSuspended(int i) {
		Toast.makeText(this, "Connection Suspended", Toast.LENGTH_LONG).show();

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Create an instance of GoogleAPIClient.
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();
		}

		if (!mGoogleApiClient.isConnected()) {
			mGoogleApiClient.connect();
		}
		Toast.makeText(this, "Location Service starting", Toast.LENGTH_SHORT).show();
		serviceStarted = true;

		startForeground(LocationService.notification_id, new LocationServiceRunningNotification(this).getNotification());

		startTime = System.nanoTime();

		mLocations = new ArrayList<>();
		currentAddress = "";
		startSynchronization();

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void stopService() {
		serviceStarted = false;
		if (mGoogleApiClient != null) {
			LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
			if (mGoogleApiClient.isConnected()) {
				mGoogleApiClient.disconnect();
			}
		}

		stopSynchronizationService();
		stopForeground(true);
		stopSelf();
		//NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		//manager.cancel(LocationService.notification_id);
		Toast.makeText(this, "Location Service Stopped.", Toast.LENGTH_SHORT).show();
	}


	public boolean isServiceStarted() {
		return serviceStarted;
	}

	@Override
	public void onDestroy() {

		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(LocationService.notification_id);
		Toast.makeText(this, "Location Service Done. Closing.", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLocationChanged(final Location location) {
		if (location.hasAccuracy()
			&& location.getAccuracy() < LocationService.min_accuracy
			//&& (mLocations.size() == 0 || (mLocations.size() > 0 && mLocations.get(0).distanceTo(location) > 250)) //minimum 100m to the past location
				) {
			mLocations.add(0, location);

			if (server_id == null) {
				LocationTasks.getOrInsertServerId(this, new OnDataSetReceivedListener() {
					@Override
					public void onDataSetReceived(Cursor cursor) {
						server_id = cursor.getString(cursor.getColumnIndexOrThrow(LocationContract.ServeridEntry.COLUMN_NAME_SERVERID));
						addLocationToSqlite(location);

						if (onHasNewLocationListener != null) {
							onHasNewLocationListener.locationAdded(location);
						}
					}
				});
			} else {
				addLocationToSqlite(location);
			}

			if (mLocations.size() > 0) {
				distance = 0;
				Location last_loc = mLocations.get(0);
				for (Location loc : mLocations) {
					if (loc != last_loc) {
						distance += last_loc.distanceTo(loc);
					}
					last_loc = loc;
				}
			}


			updateNotification();
			if (onHasNewLocationListener != null) {
				onHasNewLocationListener.locationAdded(location);
			}

		}

	}

	private void addLocationToSqlite(final Location location) {
		if(DataConnection.isNetworkAvailable(this)) {
			new GetCityName().getCityName(this, location, new OnGeocoderFinishedListener() {
				@Override
				public void onFinished(List<Address> results) {
					if (results == null) {
						currentAddress = "No address found.";
					} else if (results.size() > 0) {
						StringBuilder builder = new StringBuilder();
						int maxLines = results.get(0).getMaxAddressLineIndex();
						for (int i = 0; i < maxLines; i++) {
							String addressStr = results.get(0).getAddressLine(i);
							builder.append(addressStr);
							builder.append(", ");
						}
						builder.append(results.get(0).getCountryName());

						currentAddress = builder.toString(); //This is the complete address.
					}
					addLocationToSqlite(location, currentAddress);
					updateNotification();

				}
			});
		} else {
			currentAddress = "No Internet Connection found. Waiting to connect...";
			addLocationToSqlite(location, currentAddress);
			updateNotification();
		}

	}

	private void addLocationToSqlite(Location location, String address) {
		LocationTasks.addLocationTask(this, location, server_id, address, new OnDataSetInsertedListener() {
			@Override
			public void onDataSetInserted(long newRowId) {
				//publish it to the world
				//you can ignore the newRowID :)
				//startSynchronization(); We don't do this anymore, we do this now in intervals. see "startSynchronization()" and "stopSynchronization()"

			}
		});
	}

	private void startSynchronization() {
		startService(new Intent(this, SynchronizationService.class));
	}

	public double getDistance() {
		return distance;
	}

	public void unsetOnHasNewLocationListener() {
		this.onHasNewLocationListener = null;
	}

	public String getServerId() {
		return this.server_id;
	}


	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public LocationService getService() {
			// Return this instance of LocalService so clients can call public methods
			return LocationService.this;
		}
	}


	private void updateNotification() {
		Notification        notification         = new LocationServiceRunningNotification(this).getNotification(currentAddress);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationManager.notify(LocationService.notification_id, notification);
	}

	private void stopSynchronizationService() {
		AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(this, SynchronizationService.class);
		PendingIntent pi = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_NO_CREATE);

		alarmMgr.cancel(pi);

		if(pi == null){
			return;
		}
		pi.cancel();//important
	}

	private void startSynchronizationService() {
		Intent intent = new Intent(this, SynchronizationService.class);
		PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);
		AlarmManager  am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pi);


		long interval = 10 * 60 * 1000;
		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
							   SystemClock.elapsedRealtime() + 10000,
							   interval, pi);
	}


}
