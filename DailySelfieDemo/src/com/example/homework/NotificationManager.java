package com.example.homework;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class NotificationManager {
	private Context mContext;
	private AlarmManager mAlarmManager;
	
	private static final long INITIAL_ALARM_DELAY = 2 * 60 * 1000L;
	private Intent mNotificationReceiverIntent;
	private PendingIntent mNotificationReceiverPendingIntent;
	
	public NotificationManager(Context context) {
		mContext = context;
		mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		
		// Create an Intent to broadcast to the AlarmNotificationReceiver
		mNotificationReceiverIntent = new Intent(mContext,
				NotificationReceiver.class);

		// Create an PendingIntent that holds the NotificationReceiverIntent
		mNotificationReceiverPendingIntent = PendingIntent.getBroadcast(mContext,
				0, mNotificationReceiverIntent, 0);
	}
	
	public void setAlarm() {
		mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + INITIAL_ALARM_DELAY,
				INITIAL_ALARM_DELAY,
				mNotificationReceiverPendingIntent);
		
		// TODO: support customized set alarm, user could specify mode, time, delay
	}
	
	public void cancelAlarm() {
		mAlarmManager.cancel(mNotificationReceiverPendingIntent);
	}
}
