package com.stuff.reminder;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private ArrayList<String> taskList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText etTask = findViewById(R.id.etTask);
        Button btnAdd = findViewById(R.id.btnAddTask);
        ListView lvTasks = findViewById(R.id.lvTasks);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskList);
        lvTasks.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            String task = etTask.getText().toString().trim();
            if (!task.isEmpty()) {
                taskList.add(task);
                adapter.notifyDataSetChanged();
                etTask.setText("");
                Toast.makeText(MainActivity.this, "Task Added", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
