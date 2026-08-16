package com.stuff.reminder;

import android.app.*;
import android.content.*;
import android.media.RingtoneManager;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("task_title");
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "stuff_urgent_final";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(channelId, "Task Reminders", NotificationManager.IMPORTANCE_HIGH);
            ch.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null);
            ch.enableVibration(true);
            nm.createNotificationChannel(ch);
        }

        Notification n = new Notification.Builder(context, channelId)
                .setContentTitle("Task Reminder")
                .setContentText(title)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true)
                .build();
        nm.notify((int)System.currentTimeMillis(), n);
    }
}
