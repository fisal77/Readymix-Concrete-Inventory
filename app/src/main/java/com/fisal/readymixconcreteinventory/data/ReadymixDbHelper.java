package com.fisal.readymixconcreteinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fisal.readymixconcreteinventory.data.ReadymixContract.ReadymixEntry;

/**
 * Database helper for Readymix concrete inventory app. Manages database creation and version management.
 */
public class ReadymixDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ReadymixDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "readymix.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ReadymixDbHelper}.
     *
     * @param context of the app
     */
    public ReadymixDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the readymix table
        String SQL_CREATE_READYMIX_TABLE =  "CREATE TABLE " + ReadymixEntry.TABLE_NAME + " ("
                + ReadymixEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ReadymixEntry.COLUMN_READYMIX_NAME + " TEXT NOT NULL, "
                + ReadymixEntry.COLUMN_READYMIX_PRICE + " INTEGER NOT NULL, "
                + ReadymixEntry.COLUMN_READYMIX_QUANTITY + " INTEGER NOT NULL DEFAULT 1, "
                // + ReadymixEntry.COLUMN_PRODUCT_IMAGE + " BLOB, "
                + ReadymixEntry.COLUMN_SUPPLIER_NAME + " TEXT, "
                + ReadymixEntry.COLUMN_SUPPLIER_EMAIL + " TEXT, "
                + ReadymixEntry.COLUMN_SUPPLIER_PHONE + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_READYMIX_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}