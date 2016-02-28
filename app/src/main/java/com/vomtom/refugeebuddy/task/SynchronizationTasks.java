package com.vomtom.refugeebuddy.task;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.vomtom.refugeebuddy.R;
import com.vomtom.refugeebuddy.listeners.OnSynchronizationFinishedListener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SynchronizationTasks {

	private static void goBlooey(Throwable t, Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder
				.setTitle("Exception!")
				.setMessage(t.toString())
				.setPositiveButton("OK", null)
				.show();
	}

	public static void uploadLocation(final Context context, final String longitude, final String latitude, final String server_id, final String bearing, final String speed, final String altitude, final String accuracy, final String time, final String ID, final OnSynchronizationFinishedListener listener) {

		new AsyncTask<Void, Integer, Void>() {

			private Exception exception = null;
			private Cursor cursor = null;


			@Override
			protected Void doInBackground(Void... params) {

				try {
					URL url = null;
					String jsonStringReturn = null;

					url = new URL("http://www.newscombinator.com/location/search/postlocation");

					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.setRequestMethod("POST");
					urlConnection.setDoInput(true);
					urlConnection.setDoOutput(true);

					Uri.Builder builder = new Uri.Builder()
							.appendQueryParameter("longitude", longitude)
							.appendQueryParameter("latitude", latitude)
							.appendQueryParameter("speed", speed)
							.appendQueryParameter("altitude", altitude)
							.appendQueryParameter("bearing", bearing)
							.appendQueryParameter("time", time)
							.appendQueryParameter("location_id", ID)
							.appendQueryParameter("server_id", server_id)
							.appendQueryParameter("accuracy", accuracy);
					String query = builder.build().getEncodedQuery();

					OutputStream os = urlConnection.getOutputStream();
					BufferedWriter writer = new BufferedWriter(
							new OutputStreamWriter(os, "UTF-8"));
					writer.write(query);
					writer.flush();
					writer.close();
					os.close();

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



				} catch (IOException e) {
					exception = e;
				}


				return null;

			}


			@Override
			protected void onPostExecute(Void args) {
				if (exception != null) {
					if (exception instanceof IOException) {
						Log.e("LocationTracker", context.getString(R.string.upload_tried_failed));
					} else {
						goBlooey(exception, context);
					}
					if(listener != null) {
						listener.onError(ID);
					}
				} else {
					if (listener != null) {
						listener.onFinished(ID);
					}
				}
			}
		}.execute();


	}

}
