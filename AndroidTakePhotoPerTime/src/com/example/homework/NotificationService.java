package com.example.homework;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class NotificationService extends Service {
	
	public static final String NOTIFY_TITLE = "Daily Selfie";
	public static final String NOTIFY_BODY = "Time for another selfie";
	
	private final String TAG = "com.example.homework.NotificationService";
	private final CharSequence tickerText = "This is a Really, Really, Super Long Notification Message!";
	private static final int MSG = 0x1234;
	private final int INTERVAL = 2 * 60 * 1000;
	private long[] mVibratePattern = { 0, 200, 200, 300 };
	
	// Notification Sound and Vibration on Arrival
	private Uri soundURI = Uri
			.parse("android.resource://com.example.homework/"
					+ R.raw.alarm_rooster);

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "onStart");
		
		final Handler messageHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == NotificationService.MSG) {
					
					Intent mNotificationIntent = new Intent(getApplicationContext(),
							MainActivity.class);
					PendingIntent mContentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
							mNotificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
					
					Notification.Builder notificationBuilder = new Notification.Builder(
							getApplicationContext())
							.setTicker(tickerText)
							.setSmallIcon(R.id.open_camera)
							.setAutoCancel(true)
							.setContentTitle(NOTIFY_TITLE)
							.setContentText(NOTIFY_BODY)
							.setContentIntent(mContentIntent)
							.setSound(soundURI)
							.setVibrate(mVibratePattern);

					// Pass the Notification to the NotificationManager:
					NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.notify(1,
							notificationBuilder.build());
				}
			}
		};
		
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				messageHandler.sendEmptyMessage(NotificationService.MSG);
			}	
		}, 0, INTERVAL);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
	}
}
