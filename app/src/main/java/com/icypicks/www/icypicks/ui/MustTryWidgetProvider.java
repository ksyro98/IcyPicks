package com.icypicks.www.icypicks.ui;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.icypicks.www.icypicks.R;

/**
 * Implementation of App Widget functionality.
 */
public class MustTryWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d("wTag", "updateAppWidget");
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.must_try_widget_layout);
//        remoteViews.setEmptyView(R.id.item_frame_layout, R.id.empty_frame_layout);

//        uncomment later
//        Intent intent = new Intent(context, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        //maybe should use setOnClickPendingIntent
//        remoteViews.setPendingIntentTemplate(R.id.widget_frame_layout, pendingIntent);

        Intent adapterIntent = new Intent(context, ListWidgetService.class);
        adapterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        adapterIntent.putExtra("Random", Math.random() * 1000); // Add a random integer to stop the Intent being ignored.  This is needed for some API levels due to intent caching
        adapterIntent.setData(Uri.parse(adapterIntent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(R.id.must_try_widget_list_view, adapterIntent);
        context.startService(adapterIntent);

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.must_try_widget_list_view);

//        Log.d("wTag", "updateAppWidget");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("wTag", "onUpdate");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
//            Intent intent = new Intent(context, ListWidgetService.class);
//            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
//            intent.putExtra("Random", Math.random()*1000); // Add a random integer to stop the Intent being ignored.  This is needed for some API levels due to intent caching
//            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
//
//            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.must_try_widget_layout);
//            remoteViews.setEmptyView(R.id.item_frame_layout, R.id.empty_frame_layout);
//            remoteViews.setRemoteAdapter(R.id.must_try_widget_list_view, intent);
//
//            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
//            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.must_try_widget_list_view);
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("wTag", "onReceive");
        super.onReceive(context, intent);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(), MustTryWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }
}

