package uk.ac.brookes.bourgein.mapsproject;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;


public class NotificationBuilder {
	private static int mNotificationId = 0;
	
	public static void showNotification(String title, String message, Context context) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.runt_notification) // change
																		// this
																		// to my
																		// icon
				.setContentTitle(title).setContentText(message);

		NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
		mNotifyMgr.notify(mNotificationId, mBuilder.build());
		mNotificationId++;
	}
}
