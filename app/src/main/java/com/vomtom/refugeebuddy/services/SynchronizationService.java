package com.vomtom.refugeebuddy.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.vomtom.refugeebuddy.contracts.LocationContract;
import com.vomtom.refugeebuddy.helper.DataConnection;
import com.vomtom.refugeebuddy.listeners.OnDatabaseOpenedListener;
import com.vomtom.refugeebuddy.listeners.OnSynchronizationFinishedListener;
import com.vomtom.refugeebuddy.task.LocationTasks;
import com.vomtom.refugeebuddy.task.SynchronizationTasks;


/**
 * Created by Thomas on 23.02.2016.
 */
public class SynchronizationService extends Service {


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		LocationTasks.openWriteableDatabase(this, new OnDatabaseOpenedListener() {
			@Override
			public void onOpened(SQLiteDatabase db) {
				startTransferringAllUntransferredLocations(db);
			}

			@Override
			public void onError(Exception exception) {
				Log.e("LocationService", exception.getMessage());
			}
		});
		return Service.START_NOT_STICKY;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * This method transferrs all untransferred locations. At this point only the newest location will be transferred to the server, all other buffered locations will be omitted.
	 * @param db
	 */
	private void startTransferringAllUntransferredLocations(final SQLiteDatabase db) {
		if(DataConnection.isNetworkAvailable(this)) {
			Cursor cursor = LocationTasks.getAllUntransferredLocations(db); //this should get all untransferred locations, ordered from the newest to the oldest (order by _ID DESC).
			boolean firstLocationTransferred = false; //we only want to transfer the newest, yet untransferred location. The rest we simply set as transferred for now.
			if (cursor.moveToFirst()) {
				do {
					if (!firstLocationTransferred) {
						firstLocationTransferred = true;
						LocationTasks.lockLoationInTransfer(db, cursor.getString(cursor.getColumnIndexOrThrow(LocationContract.LocationEntry._ID))); //lock in case another instance is trying to do that.
						SynchronizationTasks.uploadLocation(SynchronizationService.this,
															cursor.getString(cursor.getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_NAME_LNG)),
															cursor.getString(cursor.getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_NAME_LAT)),
															cursor.getString(cursor.getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_NAME_SERVERID)),
															cursor.getString(cursor.getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_NAME_BEARING)),
															cursor.getString(cursor.getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_NAME_SPEED)),
															cursor.getString(cursor.getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_NAME_ALTITUDE)),
															cursor.getString(cursor.getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_NAME_ACCURACY)),
															cursor.getString(cursor.getColumnIndexOrThrow(LocationContract.LocationEntry.COLUMN_NAME_CREATED)),
															cursor.getString(cursor.getColumnIndexOrThrow(LocationContract.LocationEntry._ID)),
															new OnSynchronizationFinishedListener() {

																@Override
																public void onFinished(String id) {
																	LocationTasks.unlockLocationTransferred(db, id, 1); //it was transferred correctly. Write that to the DB.
																}

																@Override
																public void onError(String id) {
																	LocationTasks.unlockLocationTransferred(db, id, 0); //Error happened, try again later.


																}

																@Override
																public void onFinished() {
																	//nothing
																}
															}
														   );
					} else {
						LocationTasks.unlockLocationTransferred(db, cursor.getString(cursor.getColumnIndexOrThrow(LocationContract.LocationEntry._ID)), 1); //sub-sequent locations are just written to transferred correctly at this point.
					}
				} while (cursor.moveToNext());
			}
		}
	}

}
