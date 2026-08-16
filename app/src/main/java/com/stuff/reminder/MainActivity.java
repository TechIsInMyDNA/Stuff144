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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.util.Calendar;

public class MainActivity extends Activity {
    private Calendar selectedTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showDialog();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stuff - Add Task / Title");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        TextView tvTitleLabel = new TextView(this);
        tvTitleLabel.setText("Widget Header Title:");
        layout.addView(tvTitleLabel);

        final EditText etHeader = new EditText(this);
        etHeader.setText(TaskStorage.getWidgetTitle(this));
        layout.addView(etHeader);

        TextView tvTaskLabel = new TextView(this);
        tvTaskLabel.setText("New Task:");
        tvTaskLabel.setPadding(0, 20, 0, 0);
        layout.addView(tvTaskLabel);

        final EditText etTask = new EditText(this);
        etTask.setHint("e.g. Vikram little joy mangana");
        layout.addView(etTask);

        final TextView tvDateTime = new TextView(this);
        tvDateTime.setText("Reminder: None");
        tvDateTime.setPadding(0, 15, 0, 15);
        layout.addView(tvDateTime);

        Button btnPickTime = new Button(this);
        btnPickTime.setText("Set Reminder Time");
        btnPickTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(MainActivity.this, (DatePicker view, int year, int month, int day) -> {
                selectedTime.set(Calendar.YEAR, year);
                selectedTime.set(Calendar.MONTH, month);
                selectedTime.set(Calendar.DAY_OF_MONTH, day);

                TimePickerDialog tpd = new TimePickerDialog(MainActivity.this, (TimePicker tView, int hour, int minute) -> {
                    selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                    selectedTime.set(Calendar.MINUTE, minute);
                    selectedTime.set(Calendar.SECOND, 0);
                    tvDateTime.setText("Reminder: " + day + "/" + (month + 1) + " " + hour + ":" + (minute < 10 ? "0" + minute : String.valueOf(minute)));
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                tpd.show();

            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });
        layout.addView(btnPickTime);

        builder.setView(layout);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        builder.show();
    }
}
