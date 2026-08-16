package com.stuff.reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("task_title");
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "stuff_loud_channel_v8";

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (soundUri == null) soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Immediate Direct Play fallback
        try {
            Ringtone r = RingtoneManager.getRingtone(context, soundUri);
            if (r != null) r.play();
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) v.vibrate(new long[]{0, 400, 200, 400}, -1);
        } catch (Exception ignored) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nm != null) {
            NotificationChannel ch = new NotificationChannel(channelId, "Task Alarm Reminders", NotificationManager.IMPORTANCE_HIGH);
            ch.enableVibration(true);
            ch.setVibrationPattern(new long[]{0, 400, 200, 400});
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
            ch.setSound(soundUri, audioAttributes);
            nm.createNotificationChannel(ch);
        }

        Intent openApp = new Intent(context, MainActivity.class);
        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) piFlags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pi = PendingIntent.getActivity(context, 0, openApp, piFlags);

        Notification.Builder builder = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                ? new Notification.Builder(context, channelId) 
                : new Notification.Builder(context);

        Notification n = builder
                .setContentTitle("P1 Urgent: " + (title != null ? title : "Task Reminder"))
                .setContentText("Tap to review or manage this task.")
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentIntent(pi)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true)
                .build();

        if (nm != null) {
            nm.notify((int) System.currentTimeMillis(), n);
        }
    }
}
