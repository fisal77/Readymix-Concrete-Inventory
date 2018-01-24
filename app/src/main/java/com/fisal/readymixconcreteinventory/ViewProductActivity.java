package com.fisal.readymixconcreteinventory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fisal.readymixconcreteinventory.data.ReadymixContract.ReadymixEntry;
import com.fisal.readymixconcreteinventory.data.ReadymixDbHelper;

/**
 * Displays list of readymix concrete that were entered and stored in the app.
 */
public class ViewProductActivity extends AppCompatActivity {

    /** Database helper that will provide us access to the database */
    private ReadymixDbHelper mDbHelper;

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

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new ReadymixDbHelper(this);
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

        TextView displayView = (TextView) findViewById(R.id.text_view_readymix);
        ImageView imageView = (ImageView) findViewById(R.id.view_readymix_image);

        try {
            // Create a header in the Text View that looks like this:
            //
            // The readymix table contains <number of rows in Cursor> readymix.
            //
            //
            // In the while loop below, iterate through the rows of the cursor and display
            // the information from each column in this order.
            displayView.setText("The readymix concrete table contains " + cursor.getCount() + " products.\n\n");
            displayView.append(ReadymixEntry._ID + " - " +
                    ReadymixEntry.COLUMN_READYMIX_NAME + " - " +
                    ReadymixEntry.COLUMN_READYMIX_PRICE + " - " +
                    ReadymixEntry.COLUMN_READYMIX_QUANTITY + " - " +
                    ReadymixEntry.COLUMN_PRODUCT_IMAGE + " - " +
                    ReadymixEntry.COLUMN_SUPPLIER_NAME + " - " +
                    ReadymixEntry.COLUMN_SUPPLIER_EMAIL + " - " +
                    ReadymixEntry.COLUMN_SUPPLIER_PHONE + "\n");

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(ReadymixEntry._ID);
            int pNameColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_READYMIX_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_READYMIX_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_READYMIX_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_PRODUCT_IMAGE);
            int sNameColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_SUPPLIER_NAME);
            int sEmailColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_SUPPLIER_EMAIL);
            int sPhoneColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_SUPPLIER_PHONE);



            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(pNameColumnIndex);
                int currentPrice = cursor.getInt(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                byte[] currentImage = cursor.getBlob(imageColumnIndex);
                String currentSupplierName = cursor.getString(sNameColumnIndex);
                String currentSupplierEmail = cursor.getString(sEmailColumnIndex);
                String currentSupplierPhone = cursor.getString(sPhoneColumnIndex);

                // Display the values from each column of the current row in the cursor in the TextView
                displayView.append(("\n" + currentID + " - " +
                        currentName + " - " +
                        currentPrice + " - " +
                        currentQuantity + " - " +
                        currentSupplierName + " - " +
                        currentSupplierEmail + " - " +
                        currentSupplierPhone));
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
        }
    }

    //get bitmap image from byte array
    private Bitmap convertToBitmap(byte[] currentImage) {
        return BitmapFactory.decodeByteArray(currentImage, 0, currentImage.length);
    }

    /**
     * Helper method to insert hardcoded readymix data into the database. For debugging purposes only.
     */
    private void insertReadymix() {
        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ReadymixEntry.COLUMN_READYMIX_NAME, "Fiber-reinforced");
        values.put(ReadymixEntry.COLUMN_READYMIX_PRICE, 120);
        values.put(ReadymixEntry.COLUMN_READYMIX_QUANTITY, 300);

        // Insert a new row for Fiber-reinforced in the database, returning the ID of that new row.
        // The first argument for db.insert() is the readymix table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the info for Fiber-reinforced.
        long newRowId = db.insert(ReadymixEntry.TABLE_NAME, null, values);

        Log.v("ViewProductActivity", "New row ID " + newRowId);
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
