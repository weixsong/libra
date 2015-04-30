package com.example.homework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SystemRebootReceiver extends BroadcastReceiver {
	
	private static final String TAG = "com.example.homework.SystemRebootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.i(TAG, "SystemRebootReceiver onReceive");
		Intent i = new Intent(context, NotificationService.class);
		context.startService(i);
	}
}
