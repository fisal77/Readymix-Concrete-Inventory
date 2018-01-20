package com.fisal.readymixconcreteinventory;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.fisal.readymixconcreteinventory.data.ReadymixContract.ReadymixEntry;
import com.fisal.readymixconcreteinventory.data.ReadymixDbHelper;

/**
 * Allows user to create a new readymix concrete product or edit an existing one.
 */
public class EditProductActivity extends AppCompatActivity {

    /** EditText field to enter the readymix's name */
    private EditText mNameEditText;

    /** EditText field to enter the readymix's price */
    private EditText mPriceEditText;

    /** EditText field to enter the readymix's quantity */
    private EditText mQuantityEditText;

    /** Spinner (dropdown menu) field to enter the readymix's supplier name */
    private Spinner mSupplierNameSpinner;

    /** EditText field to enter the readymix's supplier email */
    private EditText mSupplierEmailEditText;

    /** EditText field to enter the readymix's supplier phone */
    private EditText mSupplierPhoneEditText;

    /** Array strings for supplier's email and phone. All both linked to the main supplier's name array.
        When user select the main array the others will be changed as same order. */
    private String[] mSupplierEmailArray;
    private String[] mSupplierPhoneArray;

    /**
     * Supplier name of the readymix concrete. The possible valid values are in the ReadymixContract.java file:
     * {@link ReadymixEntry#OTHER_SUPPLIER}, {@link ReadymixEntry#SRMCC_SUPPLIER},
     * {@link ReadymixEntry#CEMEX_SUPPLIER}, {@link ReadymixEntry#UBINTO_SUPPLIER},
     * {@link ReadymixEntry#BINLADEN_SUPPLIER}.
     */
    private String mSupplierName = ReadymixEntry.OTHER_SUPPLIER;
    //private String mSupplierEmail;
    //private String mSupplierPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_readymix_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_readymix_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_readymix_quantity);
        mSupplierNameSpinner = (Spinner) findViewById(R.id.spinner_supplier_name);
        mSupplierEmailEditText = (EditText) findViewById(R.id.edit_supplier_email);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.edit_supplier_phone);

        // Array strings for supplier's email and phone. All both linked to the main supplier's name array.
        // When user select the main array the others will be changed as same order.
        Resources res = getResources();
        mSupplierEmailArray = res.getStringArray(R.array.array_supplier_email_options);
        mSupplierPhoneArray = res.getStringArray(R.array.array_supplier_phone_options);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the supplier details of the readymix.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter supplierNameSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_supplier_name_options, android.R.layout.simple_spinner_item);

        // Array adapters for supplier's email and phone. All both linked to the main supplier's name array adapter.
        // When user select the main array the others will be changed as same order.
        final ArrayAdapter supplierEmailEditTextAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_supplier_email_options, android.R.layout.simple_list_item_1);
        final ArrayAdapter supplierPhoneEditTextAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_supplier_phone_options, android.R.layout.simple_list_item_1);

        // Specify dropdown layout style - simple list view with 1 item per line
        supplierNameSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSupplierNameSpinner.setAdapter(supplierNameSpinnerAdapter);

        // Set the string mSelected to the constant values
        mSupplierNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.other_supplier))) {
                        mSupplierName = ReadymixEntry.OTHER_SUPPLIER;
                        mSupplierEmailEditText.setText(supplierEmailEditTextAdapter.getItem(position).toString());
                        mSupplierPhoneEditText.setText(supplierPhoneEditTextAdapter.getItem(position).toString());
                    }
                    else if (selection.equals(getString(R.string.srmcc_supplier))) {
                        mSupplierName = ReadymixEntry.SRMCC_SUPPLIER;
                        mSupplierEmailEditText.setText(supplierEmailEditTextAdapter.getItem(position).toString());
                        mSupplierPhoneEditText.setText(supplierPhoneEditTextAdapter.getItem(position).toString());
                    }
                    else if (selection.equals(getString(R.string.cemex_supplier))) {
                        mSupplierName = ReadymixEntry.CEMEX_SUPPLIER;
                        mSupplierEmailEditText.setText(supplierEmailEditTextAdapter.getItem(position).toString());
                        mSupplierPhoneEditText.setText(supplierPhoneEditTextAdapter.getItem(position).toString());
                    }
                    else if (selection.equals(getString(R.string.ubinto_supplier))) {
                        mSupplierName = ReadymixEntry.UBINTO_SUPPLIER;
                        mSupplierEmailEditText.setText(supplierEmailEditTextAdapter.getItem(position).toString());
                        mSupplierPhoneEditText.setText(supplierPhoneEditTextAdapter.getItem(position).toString());
                    }
                    else {
                        mSupplierName = ReadymixEntry.BINLADEN_SUPPLIER;
                        mSupplierEmailEditText.setText(supplierEmailEditTextAdapter.getItem(position).toString());
                        mSupplierPhoneEditText.setText(supplierPhoneEditTextAdapter.getItem(position).toString());
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplierName = ReadymixEntry.OTHER_SUPPLIER;
            }
        });
    }

    /**
     * Get user input from editor and save new readymix concrete product into database.
     */
    private void insertReadymix() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        int price = Integer.parseInt(priceString);
        String quantityString = mQuantityEditText.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);
        String supplierEmailString = mSupplierEmailEditText.getText().toString();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString();


        // Create database helper
        ReadymixDbHelper mDbHelper = new ReadymixDbHelper(this);

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and readymix attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ReadymixEntry.COLUMN_READYMIX_NAME, nameString);
        values.put(ReadymixEntry.COLUMN_READYMIX_PRICE, price);
        values.put(ReadymixEntry.COLUMN_READYMIX_QUANTITY, quantity);
        values.put(ReadymixEntry.COLUMN_READYMIX_NAME, mSupplierName);
        values.put(ReadymixEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailString);
        values.put(ReadymixEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneString);

        // Insert a new row for readymix in the database, returning the ID of that new row.
        long newRowId = db.insert(ReadymixEntry.TABLE_NAME, null, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newRowId == -1) {
            // If the row ID is -1, then there was an error with insertion.
            Toast.makeText(this, "Error with saving new product", Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(this, "New product saved with row id: " + newRowId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_edit_product.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_edit_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save new readymix product to database
                insertReadymix();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
