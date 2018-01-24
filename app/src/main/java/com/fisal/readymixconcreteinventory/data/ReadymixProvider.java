package com.fisal.readymixconcreteinventory.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by fisal on 24/01/2018.
 */

/**
 * {@link ContentProvider} for Readymix comcrete products app.
 */
public class ReadymixProvider extends ContentProvider {

    /** Database helper object */
    private ReadymixDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ReadymixDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
