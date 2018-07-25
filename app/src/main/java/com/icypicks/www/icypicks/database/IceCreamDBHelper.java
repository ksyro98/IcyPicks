package com.icypicks.www.icypicks.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class IceCreamDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "iceCreamDatabase.db";

    private static final int DATABASE_VERSION = 1;

    IceCreamDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ICE_CREAM_TABLE =
                "CREATE TABLE " + IceCreamContract.IceCreamEntry.TABLE_NAME + " ("
                + IceCreamContract.IceCreamEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + IceCreamContract.IceCreamEntry.ICE_CREAM_FLAVOR + " TEXT NOT NULL, "
                + IceCreamContract.IceCreamEntry.ICE_CREAM_PLACE + " TEXT NOT NULL, "
                + IceCreamContract.IceCreamEntry.ICE_CREAM_DESCRIPTION + " TEXT NOT NULL, "
                + IceCreamContract.IceCreamEntry.ICE_CREAM_IMAGE + " BLOB NOT NULL, "
                + IceCreamContract.IceCreamEntry.ICE_CREAM_UPLOAD_NUMBER + " INTEGER NOT NULL"
                + ");";

        db.execSQL(SQL_CREATE_ICE_CREAM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + IceCreamContract.IceCreamEntry.TABLE_NAME);
        onCreate(db);
    }
}
