package com.stuff.reminder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import java.util.List;

public class StuffWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StuffFactory(this.getApplicationContext());
    }
}

class StuffFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private List<TaskStorage.Item> taskList;

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
            views.setTextColor(R.id.tvTaskText, Color.parseColor("#3BD16F"));
        } else {
            views.setTextViewText(R.id.tvTaskText, item.text);
            views.setTextColor(R.id.tvTaskText, Color.parseColor("#EFE68C"));
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
