package com.vomtom.refugeebuddy.task;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;

import com.vomtom.refugeebuddy.contracts.LocationContract;
import com.vomtom.refugeebuddy.dbhelper.LocationDbHelper;
import com.vomtom.refugeebuddy.listeners.OnDataSetInsertedListener;
import com.vomtom.refugeebuddy.listeners.OnDataSetReceivedListener;
import com.vomtom.refugeebuddy.listeners.OnDatabaseOpenedListener;
import com.vomtom.refugeebuddy.task.exception.NoServerIdFoundException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class LocationTasks {

	private static void goBlooey(Throwable t, Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder
				.setTitle("Exception!")
				.setMessage(t.toString())
				.setPositiveButton("OK", null)
				.show();
	}

	public static void openWriteableDatabase(final Context context, final OnDatabaseOpenedListener listener) {

		new AsyncTask<Void, Integer, SQLiteDatabase>() {

			private Exception exception = null;

			@Override
			protected SQLiteDatabase doInBackground(Void... params) {
				SQLiteDatabase db = null;
				try {
					LocationDbHelper locationDbHelper = new LocationDbHelper(context);
					// Gets the data repository in write mode
					db = locationDbHelper.getWritableDatabase();
				} catch (SQLiteCantOpenDatabaseException e) {
					exception = e;
				}

				return db;


			}


			@Override
			protected void onPostExecute(SQLiteDatabase db) {
				if (listener == null) {
					goBlooey(exception, context);
				} else {
					if (exception != null) {
						listener.onError(exception);
					} else {
						listener.onOpened(db);
					}
				}
			}
		}.execute();

	}

	public static void openReadableDb(final Context context, final OnDatabaseOpenedListener listener) {

		new AsyncTask<Void, Integer, SQLiteDatabase>() {

			private Exception exception = null;

			@Override
			protected SQLiteDatabase doInBackground(Void... params) {
				SQLiteDatabase db = null;
				try {
					LocationDbHelper locationDbHelper = new LocationDbHelper(context);
					// Gets the data repository in write mode
					db = locationDbHelper.getReadableDatabase();
				} catch (SQLiteCantOpenDatabaseException e) {
					exception = e;
				}

				return db;


			}


			@Override
			protected void onPostExecute(SQLiteDatabase db) {
				if (listener == null) {
					goBlooey(exception, context);
				} else {
					if (exception != null) {
						listener.onError(exception);
					} else {
						listener.onOpened(db);
					}
				}
			}
		}.execute();

	}

	public static void getAllLocations(final Context context, final String server_id, final OnDataSetReceivedListener listener) {

		new AsyncTask<Void, Integer, Cursor>() {

			private Exception exception = null;
			private Cursor cursor = null;

			@Override
			protected Cursor doInBackground(Void... params) {
				LocationDbHelper locationDbHelper = new LocationDbHelper(context);
				// Gets the data repository in write mode
				SQLiteDatabase db = locationDbHelper.getWritableDatabase();
				// Define a projection that specifies which columns from the database
				// you will actually use after this query.
				String[] projection = {
						LocationContract.LocationEntry._ID,
						LocationContract.LocationEntry.COLUMN_NAME_LAT,
						LocationContract.LocationEntry.COLUMN_NAME_LNG,
						LocationContract.LocationEntry.COLUMN_NAME_ACCURACY,
						LocationContract.LocationEntry.COLUMN_NAME_ADDRESS,
						LocationContract.LocationEntry.COLUMN_NAME_ALTITUDE,
						LocationContract.LocationEntry.COLUMN_NAME_BEARING,
						LocationContract.LocationEntry.COLUMN_NAME_SPEED,
						LocationContract.LocationEntry.COLUMN_NAME_TIME,
						LocationContract.LocationEntry.COLUMN_NAME_TRANSFERRED,
				};

				String sortOrder =
						LocationContract.LocationEntry._ID + " DESC";

				String[] where = {server_id};
				cursor = db.query(LocationContract.LocationEntry.TABLE_NAME, projection, LocationContract.LocationEntry.COLUMN_NAME_SERVERID + " = ?", where, null, null, sortOrder);
				return cursor;

			}


			@Override
			protected void onPostExecute(Cursor cursor) {
				if (exception != null) {
					goBlooey(exception, context);
				} else if (cursor != null && listener != null) {
					listener.onDataSetReceived(cursor);
				}
			}
		}.execute();


	}

	public static Cursor getAllUntransferredLocations(SQLiteDatabase readableDatabase) {

		String[] projection = {
				LocationContract.LocationEntry._ID,
				LocationContract.LocationEntry.COLUMN_NAME_LAT,
				LocationContract.LocationEntry.COLUMN_NAME_LNG,
				LocationContract.LocationEntry.COLUMN_NAME_ACCURACY,
				LocationContract.LocationEntry.COLUMN_NAME_ADDRESS,
				LocationContract.LocationEntry.COLUMN_NAME_ALTITUDE,
				LocationContract.LocationEntry.COLUMN_NAME_BEARING,
				LocationContract.LocationEntry.COLUMN_NAME_SPEED,
				LocationContract.LocationEntry.COLUMN_NAME_TIME,
				LocationContract.LocationEntry.COLUMN_NAME_TRANSFERRED,
				LocationContract.LocationEntry.COLUMN_NAME_SERVERID,
				LocationContract.LocationEntry.COLUMN_NAME_CREATED,
		};


		String[] where  = {"0"};
		Cursor   cursor = readableDatabase.query(LocationContract.LocationEntry.TABLE_NAME, projection, LocationContract.LocationEntry.COLUMN_NAME_TRANSFERRED + " = ?", where, null, null, LocationContract.LocationEntry._ID + " DESC");
		return cursor;


	}

	public static void unlockLocationTransferred(SQLiteDatabase db, final String ID, final int flag_transferred) {

		String[] whereValues = {ID};
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(LocationContract.LocationEntry.COLUMN_NAME_TRANSFERRED, flag_transferred);
		db.update(LocationContract.LocationEntry.TABLE_NAME, values, "_id = ?", whereValues);


	}

	public static void lockLoationInTransfer(SQLiteDatabase db, final String ID) {
		String[] whereValues = {ID};
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(LocationContract.LocationEntry.COLUMN_NAME_TRANSFERRED, 2);
		db.update(LocationContract.LocationEntry.TABLE_NAME, values, "_id = ?", whereValues);
	}

	public static void getOrInsertServerId(final Context context, final OnDataSetReceivedListener listener) {


		new AsyncTask<Void, Integer, Cursor>() {

			private Exception exception = null;
			private Cursor cursor = null;

			@Override
			protected Cursor doInBackground(Void... params) {
				LocationDbHelper locationDbHelper = new LocationDbHelper(context);
				// Gets the data repository in write mode
				SQLiteDatabase db = locationDbHelper.getWritableDatabase();
				// Define a projection that specifies which columns from the database
				// you will actually use after this query.
				String[] projection = {
						LocationContract.ServeridEntry._ID,
						LocationContract.ServeridEntry.COLUMN_NAME_SERVERID,
						LocationContract.ServeridEntry.COLUMN_NAME_SERVERKEY
				};

				String sortOrder =
						LocationContract.ServeridEntry._ID + " DESC";


				cursor = db.query(LocationContract.ServeridEntry.TABLE_NAME, projection, null, null, null, null, sortOrder, "1");
				if (!cursor.moveToFirst()) {
					exception = new NoServerIdFoundException("No Server ID found. Insert one first.");
				}
				return cursor;

			}


			@Override
			protected void onPostExecute(Cursor cursor) {
				if (exception != null) {
					if (exception instanceof NoServerIdFoundException) {
						/**
						 * I would love to call that in the "doInBackground" method, but cannot, as another async task can only be started from the UI Thread. So, we have to do that in the onPostExecute thing.
						 *
						 * The way of checking if an exception was blowing up, is from http://stackoverflow.com/questions/1739515/asynctask-and-error-handling-on-android
						 * https://github.com/commonsguy/cw-lunchlist/tree/master/15-Internet/LunchList
						 */
						insertNewServerId(context, new OnDataSetInsertedListener() {
							@Override
							public void onDataSetInserted(long newRowId) {
								//a new Server ID was inserted. Whatever the Row-ID is, we don't care, we just want to have the last server id.
								getOrInsertServerId(context, listener);
							}
						});
					} else {
						goBlooey(exception, context);
					}
				} else if (cursor != null && listener != null) {
					listener.onDataSetReceived(cursor);
				}
			}
		}.execute();


	}


	public static void insertNewServerId(final Context context, final OnDataSetInsertedListener listener) {
		new AsyncTask<Void, Integer, Long>() {

			private Exception exception = null;
			private Long db_id = null;

			@Override
			protected Long doInBackground(Void... params) {
				URL    url              = null;
				String jsonStringReturn = null;
				try {
					url = new URL("http://www.newscombinator.com/location/search");

					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.setRequestMethod("GET");

					urlConnection.setReadTimeout(2000);
					urlConnection.connect();
					// read the output from the server
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
					StringBuilder stringBuilder = new StringBuilder();

					String line = null;
					while ((line = bufferedReader.readLine()) != null) {
						stringBuilder.append(line + "\n");
					}
					jsonStringReturn = stringBuilder.toString();
					try {
						InputStream in = new BufferedInputStream(urlConnection.getInputStream());
					} finally {
						urlConnection.disconnect();
					}


					JSONObject jObject = new JSONObject(jsonStringReturn);
					LocationDbHelper locationDbHelper = new LocationDbHelper(context);
					// Gets the data repository in write mode
					SQLiteDatabase db = locationDbHelper.getWritableDatabase();

					// Create a new map of values, where column names are the keys
					ContentValues values = new ContentValues();
					values.put(LocationContract.ServeridEntry.COLUMN_NAME_SERVERID, jObject.getString("smartphones_id")); //yes, I know its an int, but Zend_Db_Select doesn't deliver it as such.
					values.put(LocationContract.ServeridEntry.COLUMN_NAME_SERVERKEY, jObject.getString("smartphone_key"));
					values.put(LocationContract.ServeridEntry.COLUMN_NAME_TIME, new Date().getTime());

					db_id = db.insert(LocationContract.ServeridEntry.TABLE_NAME, "null", values);

				} catch (MalformedURLException e) {
					exception = e;
				} catch (IOException e) {
					exception = e;
				} catch (JSONException e) {
					exception = e;
				}

				return db_id;


			}

			@Override
			protected void onPostExecute(Long newRowId) {
				if (newRowId != null && listener != null) {
					listener.onDataSetInserted(newRowId);
				}
			}

		}.execute();
	}

	public static void addLocationTask(final Context context, final Location location, final String serverid, final String address, final OnDataSetInsertedListener listener) {
		new AsyncTask<Void, Integer, Long>() {
			@Override
			protected Long doInBackground(Void... arg0) {
				LocationDbHelper locationDbHelper = new LocationDbHelper(context);
				// Gets the data repository in write mode
				SQLiteDatabase db = locationDbHelper.getWritableDatabase();

				// Create a new map of values, where column names are the keys
				ContentValues values = new ContentValues();
				values.put(LocationContract.LocationEntry.COLUMN_NAME_ACCURACY, location.getAccuracy());
				values.put(LocationContract.LocationEntry.COLUMN_NAME_ALTITUDE, location.getAltitude());
				values.put(LocationContract.LocationEntry.COLUMN_NAME_BEARING, location.getBearing());
				values.put(LocationContract.LocationEntry.COLUMN_NAME_LAT, location.getLatitude());
				values.put(LocationContract.LocationEntry.COLUMN_NAME_LNG, location.getLongitude());
				values.put(LocationContract.LocationEntry.COLUMN_NAME_SERVERID, serverid);
				values.put(LocationContract.LocationEntry.COLUMN_NAME_TRANSFERRED, 0);
				values.put(LocationContract.LocationEntry.COLUMN_NAME_ADDRESS, address);
				values.put(LocationContract.LocationEntry.COLUMN_NAME_TIME, "NOW()");

				return db.insert(LocationContract.LocationEntry.TABLE_NAME, "null", values);
			}


			@Override
			protected void onPostExecute(Long newRowId) {
				if (newRowId != null && listener != null) {
					listener.onDataSetInserted(newRowId);
				}
			}
		}.execute();
	}
}
