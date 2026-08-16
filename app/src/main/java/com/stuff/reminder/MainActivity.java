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
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity {
    private Calendar selectedTime = Calendar.getInstance();
    private ListView taskListView;
    private List<TaskStorage.Item> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
            }
        }

        // Layout setup
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(30, 30, 30, 30);

        Button btnAdd = new Button(this);
        btnAdd.setText("+ Add New Task");
        btnAdd.setOnClickListener(v -> showTaskDialog());
        mainLayout.addView(btnAdd);

        taskListView = new ListView(this);
        mainLayout.addView(taskListView);
        
        setContentView(mainLayout);
        refreshTaskList();
    }

    private void refreshTaskList() {
        tasks = TaskStorage.getTasks(this);
        ArrayAdapter<TaskStorage.Item> adapter = new ArrayAdapter<TaskStorage.Item>(this, android.R.layout.simple_list_item_1, tasks) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                TaskStorage.Item item = tasks.get(position);
                view.setText(item.done ? "✓ " + item.text : "☐ " + item.text);
                view.setTextColor(item.done ? Color.GRAY : Color.BLACK);
                return view;
            }
        };
        taskListView.setAdapter(adapter);
        taskListView.setOnItemClickListener((parent, view, position, id) -> {
            TaskStorage.toggleTask(this, tasks.get(position).id);
            refreshTaskList();
            // Update Widget too
            AppWidgetManager mgr = AppWidgetManager.getInstance(this);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(this, StuffWidgetProvider.class));
            mgr.notifyAppWidgetViewDataChanged(ids, R.id.lvWidgetTasks);
        });
    }

    private void showTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Task");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText etTask = new EditText(this);
        etTask.setHint("Task Name...");
        layout.addView(etTask);
        Button btnTime = new Button(this);
        btnTime.setText("Set Reminder");
        final TextView tv = new TextView(this);
        btnTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                selectedTime.set(y, m, d);
                new TimePickerDialog(this, (tView, h, min) -> {
                    selectedTime.set(h, min);
                    tv.setText("Time: " + h + ":" + min);
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
        });
        layout.addView(btnTime);
        layout.addView(tv);
        builder.setView(layout);
        builder.setPositiveButton("Save", (d, w) -> {
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
                refreshTaskList();
            }
        });
        builder.show();
    }
}
