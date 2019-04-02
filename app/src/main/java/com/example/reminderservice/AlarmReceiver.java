package com.example.reminderservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // get id & message from intent.

        int notificationId = intent.getIntExtra("notificationId", 0);
        String message = intent.getStringExtra("todo");

        //when notification is tapped call MainActivity

        Intent maninIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0 ,maninIntent, 0);

        NotificationManager myNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Prepare notification
        NotificationCompat.Builder builder;
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = myNotificationManager.getNotificationChannel("my_reminder_notification_id");
        if (mChannel == null) {
            mChannel = new NotificationChannel("my_reminder_notification_id", "my_reminder_notification_title", importance);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            myNotificationManager.createNotificationChannel(mChannel);
        }
        builder = new NotificationCompat.Builder(context, "my_reminder_notification_id");
        intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
        builder.setContentTitle("It's Time")                            // required
                .setSmallIcon(android.R.drawable.ic_dialog_info)   // required
                .setContentText(message) // required
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(contentIntent)
                .setTicker("Its' Time");

        // Notify
        myNotificationManager.notify(notificationId, builder.build());
    }
}
