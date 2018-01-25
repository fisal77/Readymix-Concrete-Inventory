package com.fisal.readymixconcreteinventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fisal.readymixconcreteinventory.data.ReadymixContract.ReadymixEntry;

/**
 * Displays list of readymix concrete that were entered and stored in the app.
 */
public class ViewProductActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the readymix product data loader */
    private static final int READYMIX_LOADER = 0;

    /** Adapter for the ListView */
    ReadymixCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product);

        // Initialize FAB to open EditProductActivity.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewProductActivity.this, EditProductActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the readymix product data
        ListView readymixListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        readymixListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of readymix product data in the Cursor.
        // There is no readymix product data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ReadymixCursorAdapter(this, null);
        readymixListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        readymixListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditProductActivity}
                Intent intent = new Intent(ViewProductActivity.this, EditProductActivity.class);

                // Form the content URI that represents the specific readymix product that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ReadymixEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.fisal.readymixconcreteinventory/readymix/7"
                // if the readymix product with ID 7 was clicked on.
                Uri currentReadymixUri = ContentUris.withAppendedId(ReadymixEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentReadymixUri);

                // Launch the {@link EditProductActivity} to display the data for the current readymix product.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(READYMIX_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded readymix data into the database. For debugging purposes only.
     */
    private void insertReadymix() {
        // Create a ContentValues object where column names are the keys,
        // and Fiber-reinforced's readymix product attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ReadymixEntry.COLUMN_READYMIX_NAME, "Fiber-reinforced");
        values.put(ReadymixEntry.COLUMN_READYMIX_PRICE, 120);
        values.put(ReadymixEntry.COLUMN_READYMIX_QUANTITY, 300);

        // Insert a new row for Fiber-reinforced product into the provider using the ContentResolver.
        // Use the {@link ReadymixEntry#CONTENT_URI} to indicate that we want to insert
        // into the readymix database table.
        // Receive the new content URI that will allow us to access Fiber-reinforced's data in the future.
        Uri newUri = getContentResolver().insert(ReadymixEntry.CONTENT_URI, values);

        Log.v("ViewProductActivity", "New row ID " + newUri);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_view_product.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_view_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertReadymix();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ReadymixEntry._ID,
                ReadymixEntry.COLUMN_READYMIX_NAME,
                ReadymixEntry.COLUMN_READYMIX_PRICE,
                ReadymixEntry.COLUMN_READYMIX_QUANTITY,
                ReadymixEntry.COLUMN_PRODUCT_IMAGE };

        //How to order the rows, formatted as an SQL ORDER BY clause.
        String orderBy = ReadymixEntry.COLUMN_READYMIX_PRICE + " DESC";

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, // Parent activity context
                ReadymixEntry.CONTENT_URI,   // Provider content URI to query
                projection,                  // Columns to include in the resulting Cursor
                null,               // The columns for the WHERE clause - Selection criteria
                null,           // The values for the WHERE clause - Selection criteria
                orderBy);                   // The sort order for the returned rows

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ReadymixCursorAdapter} with this new cursor containing updated readymix product data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
