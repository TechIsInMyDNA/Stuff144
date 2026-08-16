package com.stuff.reminder;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.util.Calendar;

public class MainActivity extends Activity {
    private int editId = -1;
    private long selectedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editId = getIntent().getIntExtra("task_id", -1);
        showInputDialog();
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(editId == -1 ? "Add Task" : "Edit Task");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        final EditText etTask = new EditText(this);
        etTask.setHint("What needs to be done?");
        layout.addView(etTask);

        // Load existing data if editing
        if (editId != -1) {
            for (TaskStorage.Item item : TaskStorage.getTasks(this)) {
                if (item.id == editId) {
                    etTask.setText(item.text);
                    selectedTime = item.dueDate;
                }
            }
        }

        Button btnDate = new Button(this);
        btnDate.setText("Set/Change Reminder");
        btnDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                c.set(y, m, d);
                new TimePickerDialog(this, (tView, h, min) -> {
                    c.set(Calendar.HOUR_OF_DAY, h);
                    c.set(Calendar.MINUTE, min);
                    selectedTime = c.getTimeInMillis();
                    Toast.makeText(this, "Time set!", Toast.LENGTH_SHORT).show();
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        layout.addView(btnDate);

        builder.setView(layout);
        builder.setPositiveButton("Save", (d, w) -> {
            String task = etTask.getText().toString();
            if (editId == -1) TaskStorage.addTask(this, task, selectedTime);
            else TaskStorage.updateTask(this, editId, task, selectedTime);
            
            // Trigger refresh
            AppWidgetManager mgr = AppWidgetManager.getInstance(this);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(this, StuffWidgetProvider.class));
            mgr.notifyAppWidgetViewDataChanged(ids, R.id.lvWidgetTasks);
            finish();
        });
        builder.show();
    }
}
