package com.stuff.reminder;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Task");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Save", (d, w) -> {
            TaskStorage.addTask(this, input.getText().toString(), 0);
            Toast.makeText(this, "Task Added", Toast.LENGTH_SHORT).show();
            finish();
        });
        builder.setNegativeButton("Cancel", (d, w) -> finish());
        builder.show();
    }
}
