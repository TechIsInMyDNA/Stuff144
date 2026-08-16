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
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setTextViewText(R.id.tvWidgetHeader, TaskStorage.getWidgetTitle(context));

            // Header Click -> Add Task (New Task)
            Intent addIntent = new Intent(context, MainActivity.class);
            addIntent.putExtra("task_id", -1);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
            PendingIntent addPi = PendingIntent.getActivity(context, 100, addIntent, flags);
            views.setOnClickPendingIntent(R.id.tvWidgetHeader, addPi);

            // Remote Adapter
            Intent svcIntent = new Intent(context, StuffWidgetService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
            views.setRemoteAdapter(R.id.lvWidgetTasks, svcIntent);

            // Item Click -> Edit Task
            Intent editIntent = new Intent(context, MainActivity.class);
            int clickFlags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) clickFlags |= PendingIntent.FLAG_MUTABLE;
            PendingIntent editPi = PendingIntent.getActivity(context, 200, editIntent, clickFlags);
            views.setPendingIntentTemplate(R.id.lvWidgetTasks, editPi);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
