package com.icypicks.www.icypicks.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
        Log.d("wTag", "onGetViewFactory");
        return new ListRemoteViewFactory(this.getApplicationContext());
    }


    class ListRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {
        private Context context;
        private ArrayList<IceCream> widgetMustTryIceCreams;

        ListRemoteViewFactory(Context context) {
            widgetMustTryIceCreams = new ArrayList<>();
            this.context = context;
//            Log.d("wTag", "constructor");
        }

        @Override
        public void onCreate() {
//            Log.d("wTag", "onCreate");
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

            Log.d("wTag", "onDataSetChanged");
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
//        if(widgetMustTryIceCreams != null){
//            return widgetMustTryIceCreams.size();
//        }
//        return 0;
            Log.d("wTag", "getCount");
            return 10;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.must_try_widget_item);

//        if(widgetMustTryIceCreams != null && widgetMustTryIceCreams.size() != 0){
//            remoteViews.setTextViewText(R.id.appwidget_text, widgetMustTryIceCreams.get(position).getFlavor());
//            Intent fillIntent = new Intent();
//            fillIntent.putExtra(ListWidgetService.INTENT_ICE_CREAM, (Parcelable) widgetMustTryIceCreams.get(position));
//            remoteViews.setOnClickFillInIntent(R.id.appwidget_text, fillIntent);
//        }
            remoteViews.setTextViewText(R.id.widget_item_text_view, "Some Text");

            Log.d("wTag", "getViewAt");

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


//    @SuppressLint("StaticFieldLeak")
//    class ListWidgetTask extends AsyncTask<Void, Void, Cursor>{
//
//        @Override
//        protected Cursor doInBackground(Void... voids) {
//            return context.getContentResolver().query(IceCreamContract.IceCreamEntry.CONTENT_URI, null, null, null, null);
//        }
//
//        @Override
//        protected void onPostExecute(Cursor cursor) {
//            super.onPostExecute(cursor);
//            super.onPostExecute(cursor);
//            if(cursor == null || !cursor.moveToFirst()){
//                return;
//            }
//
//            do{
//                String flavor = cursor.getString(cursor.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_FLAVOR));
//                String place = cursor.getString(cursor.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_PLACE));
//                String description = cursor.getString(cursor.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_DESCRIPTION));
//                byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_IMAGE));
//                int uploadNumber = cursor.getInt(cursor.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_UPLOAD_NUMBER));
//                IceCream iceCream = new IceCream(flavor, place, description, null);
//                iceCream.setUploadNumber(uploadNumber);
//                iceCream.setImageBytes(imageBytes);
//                widgetMustTryIceCreams.add(iceCream);
//            }while (cursor.moveToNext());
//
//            Log.d("wTag", "Read SQLite data.");
//        }
//    }
    }
}
