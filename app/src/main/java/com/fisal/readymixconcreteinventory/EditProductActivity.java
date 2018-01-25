package com.fisal.readymixconcreteinventory;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.fisal.readymixconcreteinventory.data.ReadymixContract.ReadymixEntry;

import java.io.ByteArrayOutputStream;

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

    /** ImageView field to enter the readymix's product image */
    private ImageView mProductImageView;
    /** Converting image to SQLite DB and store it */
    private Bitmap mBitmap;
    private byte[] mPhoto;

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

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new readymix product or editing an existing one.
        Intent intent = getIntent();
        Uri currentReadymixUri = intent.getData();

        // If the intent DOES NOT contain a readymix product content URI, then we know that we are
        // creating a new readymix product.
        if (currentReadymixUri == null) {
            // This is a new readymix product, so change the app bar to say "Add a new readymix product"
            setTitle(getString(R.string.editor_activity_title_new_readymix_product));
        } else {
            // Otherwise this is an existing readymix product, so change app bar to say "Edit Readymix product"
            setTitle(getString(R.string.editor_activity_title_edit_readymix_product));
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_readymix_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_readymix_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_readymix_quantity);
        mProductImageView = (ImageView) findViewById(R.id.edit_readymix_image);
        mSupplierNameSpinner = (Spinner) findViewById(R.id.spinner_supplier_name);
        mSupplierEmailEditText = (EditText) findViewById(R.id.edit_supplier_email);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.edit_supplier_phone);

        // Array strings for supplier's email and phone. All both linked to the main supplier's name array.
        // When user select the main array the others will be changed as same order.
        Resources res = getResources();
        mSupplierEmailArray = res.getStringArray(R.array.array_supplier_email_options);
        mSupplierPhoneArray = res.getStringArray(R.array.array_supplier_phone_options);

        setupSpinner();

        mProductImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
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

    public void selectImage(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 2);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 2:
                if(resultCode == RESULT_OK){
                    Uri choosenImage = data.getData();

                    if(choosenImage !=null){

                        mBitmap = decodeUri(choosenImage, 400);
                        mProductImageView.setImageBitmap(mBitmap);
                    }
                }
        }
    }


    //COnvert and resize our image to 400dp for faster uploading our images to DB
    protected Bitmap decodeUri(Uri selectedImage, int REQUIRED_SIZE) {

        try {

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

            // The new size we want to scale to
            // final int REQUIRED_SIZE =  size;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //Convert bitmap to bytes
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private byte[] imageToDB(Bitmap b){

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 0, bos);
        return bos.toByteArray();

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
        if (!(mBitmap == null)) {
            mPhoto = imageToDB(mBitmap);
        } else {
            mPhoto = null;
        }

        String supplierEmailString = mSupplierEmailEditText.getText().toString();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString();

        // Create a ContentValues object where column names are the keys,
        // and readymix attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ReadymixEntry.COLUMN_READYMIX_NAME, nameString);
        values.put(ReadymixEntry.COLUMN_READYMIX_PRICE, price);
        values.put(ReadymixEntry.COLUMN_READYMIX_QUANTITY, quantity);
        values.put(ReadymixEntry.COLUMN_PRODUCT_IMAGE, mPhoto);
        values.put(ReadymixEntry.COLUMN_READYMIX_NAME, mSupplierName);
        values.put(ReadymixEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailString);
        values.put(ReadymixEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneString);

        // Insert a new readymix into the provider, returning the content URI for the new readymix product.
        Uri newUri = getContentResolver().insert(ReadymixEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editProduct_insert_new_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editProduct_insert_new_successful),
                    Toast.LENGTH_SHORT).show();
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
