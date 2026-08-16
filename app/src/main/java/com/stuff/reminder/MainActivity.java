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
import androidx.core.app.ActivityCompat;
import java.util.Calendar;

public class MainActivity extends Activity {
    private Calendar selectedTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Android 12+ Runtime Permission Check
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
        builder.setTitle("Add Task / Update Title");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        final EditText etHeader = new EditText(this);
        etHeader.setHint("Widget Header Name");
        etHeader.setText(TaskStorage.getWidgetTitle(this));
        layout.addView(etHeader);

        final EditText etTask = new EditText(this);
        etTask.setHint("New Task...");
        layout.addView(etTask);

        final TextView tvDateInfo = new TextView(this);
        tvDateInfo.setText("Reminder: None");
        layout.addView(tvDateInfo);

        Button btnSet = new Button(this);
        btnSet.setText("Set Date/Time");
        btnSet.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                selectedTime.set(y, m, d);
                new TimePickerDialog(this, (tView, h, min) -> {
                    selectedTime.set(Calendar.HOUR_OF_DAY, h);
                    selectedTime.set(Calendar.MINUTE, min);
                    tvDateInfo.setText("Reminder: " + d + "/" + (m + 1) + " " + h + ":" + min);
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
        });
        layout.addView(btnSet);

        builder.setView(layout);
        builder.setPositiveButton("Save", (d, w) -> {
            TaskStorage.setWidgetTitle(this, etHeader.getText().toString());
            String task = etTask.getText().toString();
            if (!task.isEmpty()) {
                TaskStorage.addTask(this, task);
                if (selectedTime.getTimeInMillis() > System.currentTimeMillis()) {
                    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(this, AlarmReceiver.class);
                    intent.putExtra("task_title", task);
                    PendingIntent pi = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    if (am != null) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, selectedTime.getTimeInMillis(), pi);
                }
            }
            // Force Refresh Widget
            AppWidgetManager mgr = AppWidgetManager.getInstance(this);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(this, StuffWidgetProvider.class));
            mgr.notifyAppWidgetViewDataChanged(ids, R.id.lvWidgetTasks);
            Toast.makeText(this, "Task Saved & Refreshed!", Toast.LENGTH_SHORT).show();
            finish();
        });
        builder.show();
    }
}
