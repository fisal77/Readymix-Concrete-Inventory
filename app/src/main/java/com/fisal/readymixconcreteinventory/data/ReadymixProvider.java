package com.fisal.readymixconcreteinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.fisal.readymixconcreteinventory.data.ReadymixContract.ReadymixEntry;

/**
 * Created by fisal on 24/01/2018.
 */

/**
 * {@link ContentProvider} for Readymix comcrete products app.
 */
public class ReadymixProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = ReadymixProvider.class.getSimpleName();

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
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case READYMIX:
                // For the READYMIX code, query the readymix table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the readymix table.
                cursor = database.query(ReadymixEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case READYMIX_ID:
                // For the READYMIX_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.fisal.readymixconcreteinventory/readymix/7",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 7 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ReadymixEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the readymix table where the _id equals 7 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ReadymixEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case READYMIX:
                return insertReadymix(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a new readymix product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertReadymix(Uri uri, ContentValues values) {

        // Check that the product name is not null
        String name = values.getAsString(ReadymixEntry.COLUMN_READYMIX_NAME);
        if (name == null) {
            throw new IllegalArgumentException("New product required a name");
        }

        // Check that the price is not null and valid
        Integer price = values.getAsInteger(ReadymixEntry.COLUMN_READYMIX_PRICE);
        if (price == null || price == 0 || price < 0) {
            throw new IllegalArgumentException("new product requires valid price");
        }

        // Check that the quantity is not null and valid
        Integer quantity = values.getAsInteger(ReadymixEntry.COLUMN_READYMIX_QUANTITY);
        if (quantity == null || quantity == 0 || quantity < 0) {
            throw new IllegalArgumentException("new product requires valid quantity");
        }

        // Check that the supplier name is not null and valid
        String supplier = values.getAsString(ReadymixEntry.COLUMN_SUPPLIER_NAME);
        if (supplier == null && !ReadymixEntry.isCorrectSupplier(supplier)) {
            throw new IllegalArgumentException("Pet requires valid weight");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new readymix product with the given values
        long id = database.insert(ReadymixEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
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
