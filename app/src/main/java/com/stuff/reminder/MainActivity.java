package com.stuff.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> tasks;
    private ArrayAdapter<String> adapter;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("StuffPrefs", MODE_PRIVATE);
        Set<String> savedTasks = prefs.getStringSet("tasks_list", new HashSet<>());
        tasks = new ArrayList<>(savedTasks);

        EditText etTask = findViewById(R.id.etTask);
        Button btnAdd = findViewById(R.id.btnAddTask);
        ListView lvTasks = findViewById(R.id.lvTasks);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tasks);
        lvTasks.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            String taskText = etTask.getText().toString().trim();
            if (!taskText.isEmpty()) {
                tasks.add(taskText);
                saveTasks();
                adapter.notifyDataSetChanged();
                scheduleReminder(taskText);
                etTask.setText("");
                Toast.makeText(this, "Task Added & Reminder Scheduled!", Toast.LENGTH_SHORT).show();
            }
        });

        lvTasks.setOnItemClickListener((parent, view, position, id) -> {
            tasks.remove(position);
            saveTasks();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Task Completed!", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveTasks() {
        prefs.edit().putStringSet("tasks_list", new HashSet<>(tasks)).apply();
    }

    private void scheduleReminder(String taskTitle) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("task_title", taskTitle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            long triggerTime = System.currentTimeMillis() + (60 * 1000); // 1 minute demo reminder
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }
}
