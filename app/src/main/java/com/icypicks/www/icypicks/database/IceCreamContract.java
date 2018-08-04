package com.icypicks.www.icypicks.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * This class represents the contract for the SQLite database.
 */
public class IceCreamContract {

    static final String AUTHORITY = "com.icypicks.www.icypicks";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    static final String PATH_ICE_CREAM = IceCreamEntry.TABLE_NAME;

    public static final class IceCreamEntry implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ICE_CREAM).build();

        static final String TABLE_NAME = "must_try_ice_creams";
        public static final String ICE_CREAM_FLAVOR = "ice_cream_flavor";
        public static final String ICE_CREAM_PLACE = "ice_cream_place_to_find";
        public static final String ICE_CREAM_DESCRIPTION = "ice_cream_description";
        public static final String ICE_CREAM_IMAGE = "ice_cream_image";
        public static final String ICE_CREAM_UPLOAD_NUMBER = "ice_cream_upload_number";
    }
}
