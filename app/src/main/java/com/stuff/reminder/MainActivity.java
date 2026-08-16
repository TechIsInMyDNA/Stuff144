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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private int editId = -1;
    private long selectedTime = 0;
    private SimpleDateFormat sdf = new SimpleDateFormat("d MMM, hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                try {
                    startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                } catch (Exception ignored) {}
            }
        }

        if (getIntent() != null) {
            editId = getIntent().getIntExtra("task_id", -1);
        }

        showTaskDialog();
    }

    private void showTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(editId == -1 ? "Add Task" : "Edit / Manage Task");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        final EditText etHeader = new EditText(this);
        if (editId == -1) {
            TextView tvH = new TextView(this);
            tvH.setText("Header Title:");
            layout.addView(tvH);
            etHeader.setText(TaskStorage.getWidgetTitle(this));
            layout.addView(etHeader);
        }

        TextView tvT = new TextView(this);
        tvT.setText("Task:");
        tvT.setPadding(0, 15, 0, 0);
        layout.addView(tvT);

        final EditText etTask = new EditText(this);
        etTask.setHint("What needs to be done?");
        layout.addView(etTask);

        if (editId != -1) {
            for (TaskStorage.Item item : TaskStorage.getTasks(this)) {
                if (item.id == editId) {
                    etTask.setText(item.text);
                    selectedTime = item.dueDate;
                    break;
                }
            }
        }

        final TextView tvDateInfo = new TextView(this);
        tvDateInfo.setText(selectedTime > 0 ? "Reminder: " + sdf.format(new Date(selectedTime)) : "No reminder set");
        tvDateInfo.setPadding(0, 15, 0, 10);
        layout.addView(tvDateInfo);

        Button btnDate = new Button(this);
        btnDate.setText("Set / Change Reminder Time");
        btnDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                c.set(Calendar.YEAR, y);
                c.set(Calendar.MONTH, m);
                c.set(Calendar.DAY_OF_MONTH, d);
                new TimePickerDialog(this, (tView, h, min) -> {
                    c.set(Calendar.HOUR_OF_DAY, h);
                    c.set(Calendar.MINUTE, min);
                    c.set(Calendar.SECOND, 0);
                    selectedTime = c.getTimeInMillis();
                    tvDateInfo.setText("Reminder: " + sdf.format(new Date(selectedTime)));
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        layout.addView(btnDate);

        if (editId != -1) {
            Button btnToggle = new Button(this);
            btnToggle.setText("Toggle Done / UnDone (Checkbox)");
            btnToggle.setOnClickListener(v -> {
                TaskStorage.toggleTask(this, editId);
                updateAllWidgets();
                Toast.makeText(this, "Status updated!", Toast.LENGTH_SHORT).show();
                finish();
            });
            layout.addView(btnToggle);

            Button btnDelete = new Button(this);
            btnDelete.setText("Delete Task");
            btnDelete.setOnClickListener(v -> {
                TaskStorage.deleteTask(this, editId);
                updateAllWidgets();
                Toast.makeText(this, "Task Deleted!", Toast.LENGTH_SHORT).show();
                finish();
            });
            layout.addView(btnDelete);
        }

        builder.setView(layout);
        builder.setPositiveButton("Save", (d, w) -> {
            if (editId == -1) {
                String title = etHeader.getText().toString().trim();
                if (!title.isEmpty()) TaskStorage.setWidgetTitle(this, title);
            }

            String task = etTask.getText().toString().trim();
            if (!task.isEmpty()) {
                if (editId == -1) {
                    TaskStorage.addTask(this, task, selectedTime);
                } else {
                    TaskStorage.updateTask(this, editId, task, selectedTime);
                }

                if (selectedTime > System.currentTimeMillis()) {
                    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(this, AlarmReceiver.class);
                    intent.putExtra("task_title", task);

                    int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
                    PendingIntent pi = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), intent, flags);

                    if (am != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, selectedTime, pi);
                        } else {
                            am.setExact(AlarmManager.RTC_WAKEUP, selectedTime, pi);
                        }
                    }
                }
            }

            updateAllWidgets();
            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
            finish();
        });

        builder.setNegativeButton("Cancel", (d, w) -> finish());
        builder.setOnCancelListener(d -> finish());
        builder.show();
    }

    private void updateAllWidgets() {
        AppWidgetManager mgr = AppWidgetManager.getInstance(this);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(this, StuffWidgetProvider.class));
        mgr.notifyAppWidgetViewDataChanged(ids, R.id.lvWidgetTasks);

        Intent updateIntent = new Intent(this, StuffWidgetProvider.class);
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(updateIntent);
    }
}
