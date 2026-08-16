package com.stuff.reminder;

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
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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
        showCustomDialog();
    }

    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stuff Task & Title");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Widget Header Name:");
        layout.addView(tvTitle);

        final EditText etHeader = new EditText(this);
        etHeader.setText(TaskStorage.getWidgetTitle(this));
        layout.addView(etHeader);

        TextView tvTask = new TextView(this);
        tvTask.setText("New Task:");
        tvTask.setPadding(0, 20, 0, 0);
        layout.addView(tvTask);

        final EditText etTask = new EditText(this);
        etTask.setHint("e.g. Important task...");
        layout.addView(etTask);

        final TextView tvDateInfo = new TextView(this);
        tvDateInfo.setText("Reminder: None");
        tvDateInfo.setPadding(0, 15, 0, 15);
        layout.addView(tvDateInfo);

        Button btnSetTime = new Button(this);
        btnSetTime.setText("Set Reminder Date/Time");
        btnSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = new DatePickerDialog(MainActivity.this, (view, year, month, day) -> {
                    selectedTime.set(Calendar.YEAR, year);
                    selectedTime.set(Calendar.MONTH, month);
                    selectedTime.set(Calendar.DAY_OF_MONTH, day);

                    TimePickerDialog tpd = new TimePickerDialog(MainActivity.this, (tView, hour, minute) -> {
                        selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                        selectedTime.set(Calendar.MINUTE, minute);
                        selectedTime.set(Calendar.SECOND, 0);
                        tvDateInfo.setText("Reminder: " + day + "/" + (month + 1) + " " + hour + ":" + (minute < 10 ? "0" + minute : minute));
                    }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                    tpd.show();
                }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
                dpd.show();
            }
        });
        layout.addView(btnSetTime);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String headerText = etHeader.getText().toString().trim();
            if (!headerText.isEmpty()) {
                TaskStorage.setWidgetTitle(MainActivity.this, headerText);
            }

            String taskText = etTask.getText().toString().trim();
            if (!taskText.isEmpty()) {
                TaskStorage.addTask(MainActivity.this, taskText);

                if (selectedTime.getTimeInMillis() > System.currentTimeMillis()) {
                    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    if (am != null) {
                        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                        intent.putExtra("task_title", taskText);
                        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            flags |= PendingIntent.FLAG_IMMUTABLE;
                        }
                        PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, (int) System.currentTimeMillis(), intent, flags);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, selectedTime.getTimeInMillis(), pi);
                        } else {
                            am.setExact(AlarmManager.RTC_WAKEUP, selectedTime.getTimeInMillis(), pi);
                        }
                    }
                }
            }

            AppWidgetManager mgr = AppWidgetManager.getInstance(MainActivity.this);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(MainActivity.this, StuffWidgetProvider.class));
            mgr.notifyAppWidgetViewDataChanged(ids, R.id.lvWidgetTasks);

            Intent updateIntent = new Intent(MainActivity.this, StuffWidgetProvider.class);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(updateIntent);

            Toast.makeText(MainActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
            finish();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> finish());
        builder.setOnCancelListener(dialog -> finish());
        builder.show();
    }
}
