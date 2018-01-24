package com.fisal.readymixconcreteinventory.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by fisal on 24/01/2018.
 */

/**
 * {@link ContentProvider} for Readymix comcrete products app.
 */
public class ReadymixProvider extends ContentProvider {

    /** URI matcher code for the content URI for the readymixes table */
    private static final int READYMIX = 1000;

    /** URI matcher code for the content URI for a single readymix in the readymix table */
    private static final int READYMIX_ID = 1001;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.fisal.readymixconcreteinventory/readymix" will map to the
        // integer code {@link #READYMIX}. This URI is used to provide access to MULTIPLE rows
        // of the readymix table.
        sUriMatcher.addURI(ReadymixContract.CONTENT_AUTHORITY, ReadymixContract.PATH_READYMIX, READYMIX);

        // The content URI of the form "content://com.fisal.readymixconcreteinventory/readymix/#" will map to the
        // integer code {@link #READYMIX_ID}. This URI is used to provide access to ONE single row
        // of the pets table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.fisal.readymixconcreteinventory/readymix/7" matches, but
        // "content://com.fisal.readymixconcreteinventory/readymix" (without a number at the end) doesn't match.
        sUriMatcher.addURI(ReadymixContract.CONTENT_AUTHORITY, ReadymixContract.PATH_READYMIX + "/#", READYMIX_ID);
    }

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
