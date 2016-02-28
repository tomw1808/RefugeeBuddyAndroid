package com.vomtom.refugeebuddy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import com.vomtom.refugeebuddy.helper.DataConnection;
import com.vomtom.refugeebuddy.services.SynchronizationService;

/**
 * Created by Thomas on 28.02.2016.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (DataConnection.isNetworkAvailable(context)) {
			context.startService(new Intent(context, SynchronizationService.class));
		}
	}


}
