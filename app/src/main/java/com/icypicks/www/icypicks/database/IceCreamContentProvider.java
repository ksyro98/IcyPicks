package com.icypicks.www.icypicks.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class IceCreamContentProvider extends ContentProvider {
    private IceCreamDBHelper iceCreamDBHelper;

    private static final int ICE_CREAMS = 100;
    //TODO use this too
    private static final int ICE_CREAM_WITH_UPLOAD_NUMBER = 101;
    private static final String TAG = IceCreamContentProvider.class.getSimpleName();

    private static final UriMatcher uriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(IceCreamContract.AUTHORITY, IceCreamContract.PATH_ICE_CREAM, ICE_CREAMS);
        uriMatcher.addURI(IceCreamContract.AUTHORITY, IceCreamContract.PATH_ICE_CREAM + "/#", ICE_CREAM_WITH_UPLOAD_NUMBER);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        iceCreamDBHelper = new IceCreamDBHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase sqLiteDatabase = iceCreamDBHelper.getReadableDatabase();
        int match = uriMatcher.match(uri);

        Cursor cursor;

        switch (match){
            case ICE_CREAMS:
                cursor = sqLiteDatabase.query(IceCreamContract.IceCreamEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return cursor;
//        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase sqLiteDatabase = iceCreamDBHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        Uri returnUri;

        switch (match){
            case ICE_CREAMS:
                long id = sqLiteDatabase.insert(IceCreamContract.IceCreamEntry.TABLE_NAME, null, values);
                if(id > 0){
                    returnUri = ContentUris.withAppendedId(IceCreamContract.IceCreamEntry.CONTENT_URI, id);
                }
                else{
                    throw new SQLException("Failed to insert new data into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = iceCreamDBHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        int returnInt;

        switch (match){
            case ICE_CREAMS:
                returnInt = sqLiteDatabase.delete(IceCreamContract.IceCreamEntry.TABLE_NAME, selection, selectionArgs);
                if(returnInt <= 0)
                    throw new SQLException("Failed to delete " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri" + uri);
        }

        return returnInt;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
