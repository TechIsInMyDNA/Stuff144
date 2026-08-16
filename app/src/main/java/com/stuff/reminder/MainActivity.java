package com.stuff.reminder;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;

public class MainActivity extends Activity {
    private Calendar selectedTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Android 13+ Notification Permission Prompt
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Android 12+ Exact Alarm Permission Check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        showDialog();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stuff Task & Title");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        final EditText etHeader = new EditText(this);
        etHeader.setText(TaskStorage.getWidgetTitle(this));
        layout.addView(etHeader);

        final EditText etTask = new EditText(this);
        etTask.setHint("New Task...");
        layout.addView(etTask);

        final TextView tvDateInfo = new TextView(this);
        tvDateInfo.setText("Reminder: None");
        tvDateInfo.setPadding(0, 15, 0, 15);
        layout.addView(tvDateInfo);

        Button btnSet = new Button(this);
        btnSet.setText("Set Date/Time");
        btnSet.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(MainActivity.this, (view, y, m, d) -> {
                selectedTime.set(Calendar.YEAR, y);
                selectedTime.set(Calendar.MONTH, m);
                selectedTime.set(Calendar.DAY_OF_MONTH, d);

                TimePickerDialog tpd = new TimePickerDialog(MainActivity.this, (tView, h, min) -> {
                    selectedTime.set(Calendar.HOUR_OF_DAY, h);
                    selectedTime.set(Calendar.MINUTE, min);
                    selectedTime.set(Calendar.SECOND, 0);
                    tvDateInfo.setText("Reminder: " + d + "/" + (m + 1) + " " + h + ":" + (min < 10 ? "0" + min : String.valueOf(min)));
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                tpd.show();
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });
        layout.addView(btnSet);

        builder.setView(layout);
        builder.setPositiveButton("Save", (d, w) -> {
            String headerVal = etHeader.getText().toString().trim();
            if (!headerVal.isEmpty()) {
                TaskStorage.setWidgetTitle(this, headerVal);
            }

            String task = etTask.getText().toString().trim();
            if (!task.isEmpty()) {
                TaskStorage.addTask(this, task);

                if (selectedTime.getTimeInMillis() > System.currentTimeMillis()) {
                    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(this, AlarmReceiver.class);
                    intent.putExtra("task_title", task);
                    
                    int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        flags |= PendingIntent.FLAG_IMMUTABLE;
                    }
                    
                    PendingIntent pi = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), intent, flags);
                    if (am != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, selectedTime.getTimeInMillis(), pi);
                        } else {
                            am.setExact(AlarmManager.RTC_WAKEUP, selectedTime.getTimeInMillis(), pi);
                        }
                    }
                }
            }

            // Force Refresh Widget
            AppWidgetManager mgr = AppWidgetManager.getInstance(this);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(this, StuffWidgetProvider.class));
            
            Intent updateIntent = new Intent(this, StuffWidgetProvider.class);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(updateIntent);

            mgr.notifyAppWidgetViewDataChanged(ids, R.id.lvWidgetTasks);
            
            Toast.makeText(this, "Saved & Scheduled!", Toast.LENGTH_SHORT).show();
            finish();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> finish());
        builder.setOnCancelListener(dialog -> finish());
        builder.show();
    }
}
