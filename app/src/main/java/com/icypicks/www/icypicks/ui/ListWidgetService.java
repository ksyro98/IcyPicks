package com.icypicks.www.icypicks.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Parcelable;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.icypicks.www.icypicks.R;
import com.icypicks.www.icypicks.database.IceCreamContract;
import com.icypicks.www.icypicks.java_classes.IceCream;

import java.util.ArrayList;

public class ListWidgetService extends RemoteViewsService {

    public static final String INTENT_ICE_CREAM = "ice_creams_widget";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewFactory(this.getApplicationContext());
    }


    class ListRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {
        private Context context;
        private ArrayList<IceCream> widgetMustTryIceCreams;

        ListRemoteViewFactory(Context context) {
            widgetMustTryIceCreams = new ArrayList<>();
            this.context = context;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            if (context != null) {
                Cursor cursor = context.getContentResolver().query(IceCreamContract.IceCreamEntry.CONTENT_URI, null, null, null, null);

                if (cursor == null || !cursor.moveToFirst()) {
                    return;
                }

                do {
                    String flavor = cursor.getString(cursor.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_FLAVOR));
                    String place = cursor.getString(cursor.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_PLACE));
                    String description = cursor.getString(cursor.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_DESCRIPTION));
                    byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_IMAGE));
                    int uploadNumber = cursor.getInt(cursor.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_UPLOAD_NUMBER));
                    IceCream iceCream = new IceCream(flavor, place, description, null);
                    iceCream.setUploadNumber(uploadNumber);
                    iceCream.setImageBytes(imageBytes);
                    widgetMustTryIceCreams.add(iceCream);
                } while (cursor.moveToNext());

                cursor.close();
            }

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
        if(widgetMustTryIceCreams != null){
            return widgetMustTryIceCreams.size();
        }
        return 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.must_try_widget_item);

            if(widgetMustTryIceCreams != null && widgetMustTryIceCreams.size() != 0){
                remoteViews.setTextViewText(R.id.widget_item_text_view, widgetMustTryIceCreams.get(position).getFlavor());
                Intent fillIntent = new Intent();
                fillIntent.putExtra(ListWidgetService.INTENT_ICE_CREAM, (Parcelable) widgetMustTryIceCreams.get(position));
                remoteViews.setOnClickFillInIntent(R.id.widget_item_text_view, fillIntent);
            }

            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
