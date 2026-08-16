package com.stuff.reminder;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

public class StuffWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_TASK_CLICK = "com.stuff.reminder.ACTION_TASK_CLICK";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setTextViewText(R.id.tvWidgetHeader, TaskStorage.getWidgetTitle(context));

            Intent addIntent = new Intent(context, MainActivity.class);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
            PendingIntent addPi = PendingIntent.getActivity(context, 0, addIntent, flags);
            views.setOnClickPendingIntent(R.id.tvWidgetHeader, addPi);

            Intent svcIntent = new Intent(context, StuffWidgetService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
            views.setRemoteAdapter(R.id.lvWidgetTasks, svcIntent);

            Intent clickIntent = new Intent(context, StuffWidgetProvider.class);
            clickIntent.setAction(ACTION_TASK_CLICK);
            int clickFlags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) clickFlags |= PendingIntent.FLAG_MUTABLE;
            PendingIntent clickPi = PendingIntent.getBroadcast(context, 0, clickIntent, clickFlags);
            views.setPendingIntentTemplate(R.id.lvWidgetTasks, clickPi);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_TASK_CLICK.equals(intent.getAction())) {
            int taskId = intent.getIntExtra("task_id", -1);
            if (taskId != -1) {
                TaskStorage.toggleTask(context, taskId);
                AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                int[] ids = mgr.getAppWidgetIds(new ComponentName(context, StuffWidgetProvider.class));
                mgr.notifyAppWidgetViewDataChanged(ids, R.id.lvWidgetTasks);
            }
        }
    }
}
