package com.stuff.reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("task_title");
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "stuff_reminders";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nm != null) {
            NotificationChannel ch = new NotificationChannel(channelId, "Task Reminders", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(ch);
        }

        Notification.Builder builder = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                ? new Notification.Builder(context, channelId) 
                : new Notification.Builder(context);

        Notification n = builder
                .setContentTitle("Stuff Reminder")
                .setContentText(taskTitle != null ? taskTitle : "Task Reminder!")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true)
                .build();

        if (nm != null) {
            nm.notify((int) System.currentTimeMillis(), n);
        }
    }
}
