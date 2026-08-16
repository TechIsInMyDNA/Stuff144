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
import android.os.Bundle;
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
        showDialog();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stuff - Add Task / Edit Title");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 10);

        // Header Title Edit
        TextView tvTitleLabel = new TextView(this);
        tvTitleLabel.setText("Widget Header Name:");
        tvTitleLabel.setTextSize(12);
        layout.addView(tvTitleLabel);

        final EditText etHeader = new EditText(this);
        etHeader.setText(TaskStorage.getWidgetTitle(this));
        layout.addView(etHeader);

        // Task input
        TextView tvTaskLabel = new TextView(this);
        tvTaskLabel.setText("New Task:");
        tvTaskLabel.setPadding(0, 20, 0, 0);
        tvTaskLabel.setTextSize(12);
        layout.addView(tvTaskLabel);

        final EditText etTask = new EditText(this);
        etTask.setHint("e.g. Complete presentation");
        layout.addView(etTask);

        // Date Time display
        final TextView tvDateTime = new TextView(this);
        tvDateTime.setText("Reminder: No reminder set");
        tvDateTime.setPadding(0, 15, 0, 15);
        layout.addView(tvDateTime);

        Button btnPickTime = new Button(this);
        btnPickTime.setText("Set Reminder Date & Time");
        btnPickTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                selectedTime.set(Calendar.YEAR, year);
                selectedTime.set(Calendar.MONTH, month);
                selectedTime.set(Calendar.DAY_OF_MONTH, day);

                new TimePickerDialog(this, (tView, hour, minute) -> {
                    selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                    selectedTime.set(Calendar.MINUTE, minute);
                    selectedTime.set(Calendar.SECOND, 0);
                    tvDateTime.setText("Reminder: " + day + "/" + (month + 1) + " at " + String.format("%02d:%02d", hour, minute));
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();

            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
        });
        layout.addView(btnPickTime);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            // Save Title
            String headerText = etHeader.getText().toString().trim();
            if (!headerText.isEmpty()) {
                TaskStorage.setWidgetTitle(this, headerText);
            }

            // Save Task if entered
            String taskText = etTask.getText().toString().trim();
            if (!taskText.isEmpty()) {
                TaskStorage.addTask(this, taskText);

                // Set Alarm
                if (selectedTime.getTimeInMillis() > System.currentTimeMillis()) {
                    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(this, AlarmReceiver.class);
                    intent.putExtra("task_title", taskText);
                    PendingIntent pi = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, selectedTime.getTimeInMillis(), pi);
                }
            }

            // Refresh Widget Layout & Tasks
            AppWidgetManager mgr = AppWidgetManager.getInstance(this);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(this, StuffWidgetProvider.class));
            
            Intent updateIntent = new Intent(this, StuffWidgetProvider.class);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(updateIntent);

            mgr.notifyAppWidgetViewDataChanged(ids, R.id.lvWidgetTasks);

            Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show();
            finish();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> finish());
        builder.setOnCancelListener(dialog -> finish());
        builder.show();
    }
}
