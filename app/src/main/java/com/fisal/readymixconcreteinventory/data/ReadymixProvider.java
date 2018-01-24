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

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ReadymixProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the readymixes table
     */
    private static final int READYMIX = 1000;

    /**
     * URI matcher code for the content URI for a single readymix in the readymix table
     */
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

    /**
     * Database helper object
     */
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
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

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
            throw new IllegalArgumentException("Supplier name is required");
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
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case READYMIX:
                return updateReadymix(uri, contentValues, selection, selectionArgs);
            case READYMIX_ID:
                // For the READYMIX_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ReadymixEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateReadymix(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update readymix product in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more readymix product).
     * Return the number of rows that were successfully updated.
     */
    private int updateReadymix(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link ReadymixEntry#COLUMN_READYMIX_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(ReadymixEntry.COLUMN_READYMIX_NAME)) {
            String name = values.getAsString(ReadymixEntry.COLUMN_READYMIX_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        // If the {@link ReadymixEntry#COLUMN_READYMIX_PRICE} key is present,
        // check that the price value is not null.
        if (values.containsKey(ReadymixEntry.COLUMN_READYMIX_PRICE)) {
            Integer price = values.getAsInteger(ReadymixEntry.COLUMN_READYMIX_PRICE);
            if (price == null || price == 0 || price < 0) {
                throw new IllegalArgumentException("new product requires valid price");
            }
        }

        // If the {@link ReadymixEntry#COLUMN_READYMIX_QUANTITY} key is present,
        // check that the quantity value is not null.
        if (values.containsKey(ReadymixEntry.COLUMN_READYMIX_QUANTITY)) {
            Integer quantity = values.getAsInteger(ReadymixEntry.COLUMN_READYMIX_QUANTITY);
            if (quantity == null || quantity == 0 || quantity < 0) {
                throw new IllegalArgumentException("new product requires valid quantity");
            }
        }

        // If the {@link ReadymixEntry#COLUMN_SUPPLIER_NAME} key is present,
        // check that the supplier name value is valid.
        if (values.containsKey(ReadymixEntry.COLUMN_SUPPLIER_NAME)) {
            String supplier = values.getAsString(ReadymixEntry.COLUMN_SUPPLIER_NAME);
            if (supplier == null && !ReadymixEntry.isCorrectSupplier(supplier)) {
                throw new IllegalArgumentException("Supplier name is required");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        return database.update(ReadymixEntry.TABLE_NAME, values, selection, selectionArgs);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case READYMIX:
                // Delete all rows that match the selection and selection args
                return database.delete(ReadymixEntry.TABLE_NAME, selection, selectionArgs);
            case READYMIX_ID:
                // Delete a single row given by the ID in the URI
                selection = ReadymixEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return database.delete(ReadymixEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * The use of MIME types is a natural consequence when you think about how a ContentProvider is accessed through URIs, i.e. something like an URL on the Internet.
     * Just like on the Internet there are MIME types like text/html for web pages and image/jpeg for .jpg images,
     * Android wants you to define a custom MIME type for any data type your ContentProvider handles.
     * <p>
     * This field defines a custom MIME type (recognizable by the type/subtype pattern).
     * <p>
     * Android suggests you use vnd.android.cursor.dir/... as the first part for any kind of "directory listing" (multiple items)
     * and vnd.android.cursor.item/... as the first part for any kind of single item.
     * <p>
     * For the subtype, it's again suggested to start it with vnd. and then add something like your reverse domain name / package name,
     * e.g. vnd.android.cursor.item/vnd.com.mydomain.myapp.mydata
     * <p>
     * To avoid all those vnd... strings in your code, there's also some constants in ContentResolver like CURSOR_DIR_BASE_TYPE and CURSOR_ITEM_BASE_TYPE.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case READYMIX:
                return ReadymixEntry.CONTENT_LIST_TYPE;
            case READYMIX_ID:
                return ReadymixEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


}
