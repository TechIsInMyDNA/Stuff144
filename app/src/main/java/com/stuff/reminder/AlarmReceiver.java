package com.stuff.reminder;

import android.app.*;
import android.content.*;
import android.media.RingtoneManager;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel("stuff_final", "Tasks", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(ch);
        }
        Notification n = new Notification.Builder(context, "stuff_final")
                .setContentTitle("Stuff Task")
                .setContentText("Task is due!")
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setPriority(Notification.PRIORITY_MAX)
                .build();
        nm.notify(1, n);
    }
}
