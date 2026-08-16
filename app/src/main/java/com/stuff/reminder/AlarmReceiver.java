package com.stuff.reminder;

import android.app.*;
import android.content.*;
import android.media.RingtoneManager;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("task_title");
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "stuff_urgent_v3"; // Changed ID

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(channelId, "Urgent Tasks", NotificationManager.IMPORTANCE_HIGH);
            ch.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null);
            ch.enableVibration(true);
            nm.createNotificationChannel(ch);
        }

        Notification n = new Notification.Builder(context, channelId)
                .setContentTitle("Task Reminder!")
                .setContentText(taskTitle)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true)
                .build();
        nm.notify((int)System.currentTimeMillis(), n);
    }
}
