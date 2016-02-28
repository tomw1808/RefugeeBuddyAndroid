package com.vomtom.refugeebuddy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vomtom.refugeebuddy.helper.DataConnection;
import com.vomtom.refugeebuddy.services.SynchronizationService;

/**
 * Created by Thomas on 28.02.2016.
 */
public class BootCompletedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		//does nothing at the moment, prepared for a Settings page where a user can choose to launch the tracking automatically.
	}


}
