package com.stuff.reminder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StuffWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StuffFactory(this.getApplicationContext());
    }
}

class StuffFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private List<TaskStorage.Item> taskList;
    private SimpleDateFormat sdf = new SimpleDateFormat("d MMM, hh:mm a", Locale.getDefault());

    public StuffFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {}

    @Override
    public void onDataSetChanged() {
        taskList = TaskStorage.getTasks(context);
    }

    @Override
    public void onDestroy() {}

    @Override
    public int getCount() {
        return taskList == null ? 0 : taskList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (taskList == null || position >= taskList.size()) return null;
        TaskStorage.Item item = taskList.get(position);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_task_item);

        if (item.done) {
            SpannableString span = new SpannableString(item.text);
            span.setSpan(new StrikethroughSpan(), 0, span.length(), 0);
            views.setTextViewText(R.id.tvTaskText, span);
            views.setTextColor(R.id.tvTaskText, Color.parseColor("#777777"));
            views.setTextViewText(R.id.tvCheckbox, "✓");
            views.setTextColor(R.id.tvCheckbox, Color.parseColor("#00E5FF"));
            views.setTextViewText(R.id.tvDueDate, "Done");
            views.setTextColor(R.id.tvDueDate, Color.parseColor("#555555"));
        } else {
            views.setTextViewText(R.id.tvTaskText, item.text);
            views.setTextColor(R.id.tvTaskText, Color.parseColor("#FFFFFF"));
            views.setTextViewText(R.id.tvCheckbox, "☐");
            views.setTextColor(R.id.tvCheckbox, Color.parseColor("#00E5FF"));

            if (item.dueDate > 0) {
                long now = System.currentTimeMillis();
                if (now > item.dueDate) {
                    long diffMin = (now - item.dueDate) / (1000 * 60);
                    String past = (diffMin < 60) ? "Past due by " + diffMin + "m" : "Past due by " + (diffMin / 60) + "h " + (diffMin % 60) + "m";
                    views.setTextViewText(R.id.tvDueDate, past);
                    views.setTextColor(R.id.tvDueDate, Color.parseColor("#FF5252"));
                } else {
                    views.setTextViewText(R.id.tvDueDate, sdf.format(new Date(item.dueDate)));
                    views.setTextColor(R.id.tvDueDate, Color.parseColor("#888888"));
                }
            } else {
                views.setTextViewText(R.id.tvDueDate, "");
            }
        }

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("task_id", item.id);
        views.setOnClickFillInIntent(R.id.item_row, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() { return null; }
    @Override
    public int getViewTypeCount() { return 1; }
    @Override
    public long getItemId(int position) { return position; }
    @Override
    public boolean hasStableIds() { return true; }
}
