package com.icypicks.www.icypicks.ui;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.icypicks.www.icypicks.R;

/**
 * Implementation of App Widget functionality.
 */
public class MustTryWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.must_try_widget_layout);

        Intent adapterIntent = new Intent(context, ListWidgetService.class);
        adapterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        adapterIntent.putExtra("Random", Math.random() * 1000); // Add a random integer to stop the Intent being ignored.  This is needed for some API levels due to intent caching
        adapterIntent.setData(Uri.parse(adapterIntent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(R.id.must_try_widget_list_view, adapterIntent);
        context.startService(adapterIntent);

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.must_try_widget_list_view);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(), MustTryWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }
}

