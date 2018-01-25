package com.fisal.readymixconcreteinventory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fisal.readymixconcreteinventory.data.ReadymixContract.ReadymixEntry;

/**
 * Displays list of readymix concrete that were entered and stored in the app.
 */
public class ViewProductActivity extends AppCompatActivity {

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the readymix concrete database.
     */
    private void displayDatabaseInfo() {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                ReadymixEntry._ID,
                ReadymixEntry.COLUMN_READYMIX_NAME,
                ReadymixEntry.COLUMN_READYMIX_PRICE,
                ReadymixEntry.COLUMN_READYMIX_QUANTITY,
                ReadymixEntry.COLUMN_PRODUCT_IMAGE,
                ReadymixEntry.COLUMN_SUPPLIER_NAME,
                ReadymixEntry.COLUMN_SUPPLIER_EMAIL,
                ReadymixEntry.COLUMN_SUPPLIER_PHONE};

        //How to order the rows, formatted as an SQL ORDER BY clause.
        String orderBy = ReadymixEntry.COLUMN_READYMIX_PRICE + " DESC";

        // Perform a query on the provider using the ContentResolver.
        // Use the {@link ReadymixEntry#CONTENT_URI} to access the readymix data.
        Cursor cursor = getContentResolver().query(
                ReadymixEntry.CONTENT_URI,   // The content URI of the words table
                projection,            // The columns to return for each row
                null,         // The columns for the WHERE clause - Selection criteria
                null,     // The values for the WHERE clause - Selection criteria
                orderBy);             // The sort order for the returned rows


//        ImageView imageView = (ImageView) findViewById(R.id.view_readymix_image);
        // Find the ListView which will be populated with the readymix product data
        ListView readymixProductListView = (ListView) findViewById(R.id.list);

        // Setup an Adapter to create a list item for each row of readymix product data in the Cursor.
        ReadymixCursorAdapter adapter = new ReadymixCursorAdapter(this, cursor);

        // Attach the adapter to the ListView.
        readymixProductListView.setAdapter(adapter);

/*            // Figure out the index of each column
            int imageColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_PRODUCT_IMAGE);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                byte[] currentImage = cursor.getBlob(imageColumnIndex);

                // Display the values from each column of the current row in the cursor in the TextView

                if (!(currentImage == null)) {
                    imageView.setImageBitmap(convertToBitmap(currentImage));
                } else {
                    imageView.setImageResource(R.drawable.ic_action_add_image);
                }

            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }*/
    }

/*    //get bitmap image from byte array
    private Bitmap convertToBitmap(byte[] currentImage) {
        return BitmapFactory.decodeByteArray(currentImage, 0, currentImage.length);
    }*/

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
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
