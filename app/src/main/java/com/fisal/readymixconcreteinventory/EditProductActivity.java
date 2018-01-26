package com.fisal.readymixconcreteinventory;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
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
import android.view.MotionEvent;
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
public class EditProductActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the readymix product data loader
     */
    private static final int EXISTING_READYMIX_LOADER = 0;

    /**
     * Content URI for the existing readymix product (null if it's a new readymix product)
     */
    private Uri mCurrentReadymixUri;

    /**
     * EditText field to enter the readymix's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the readymix's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the readymix's quantity
     */
    private EditText mQuantityEditText;

    /**
     * ImageView field to enter the readymix's product image
     */
    private ImageView mProductImageView;
    /**
     * Converting image from/to SQLite DB and store it
     */
    private Bitmap mBitmap;
    private byte[] mPhoto;

    /**
     * Spinner (dropdown menu) field to enter the readymix's supplier name
     */
    private Spinner mSupplierNameSpinner;

    /**
     * EditText field to enter the readymix's supplier email
     */
    private EditText mSupplierEmailEditText;

    /**
     * EditText field to enter the readymix's supplier phone
     */
    private EditText mSupplierPhoneEditText;

    /**
     * Array strings for supplier's email and phone. All both linked to the main supplier's name array.
     * When user select the main array the others will be changed as same order.
     */
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

    /** Boolean flag that keeps track of whether the readymix product has been edited (true) or not (false) */
    private boolean mReadymixProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mReadymixProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mReadymixProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new readymix product or editing an existing one.
        Intent intent = getIntent();
        mCurrentReadymixUri = intent.getData();

        // If the intent DOES NOT contain a readymix product content URI, then we know that we are
        // creating a new readymix product.
        if (mCurrentReadymixUri == null) {
            // This is a new readymix product, so change the app bar to say "Add a new readymix product"
            setTitle(getString(R.string.editor_activity_title_new_readymix_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a readymix product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing readymix product, so change app bar to say "Edit Readymix product"
            setTitle(getString(R.string.editor_activity_title_edit_readymix_product));

            // Initialize a loader to read the readymix product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_READYMIX_LOADER, null, this);
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

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the Edit Product Activity without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mProductImageView.setOnTouchListener(mTouchListener);
        mSupplierNameSpinner.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);

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
                    } else if (selection.equals(getString(R.string.srmcc_supplier))) {
                        mSupplierName = ReadymixEntry.SRMCC_SUPPLIER;
                        mSupplierEmailEditText.setText(supplierEmailEditTextAdapter.getItem(position).toString());
                        mSupplierPhoneEditText.setText(supplierPhoneEditTextAdapter.getItem(position).toString());
                    } else if (selection.equals(getString(R.string.cemex_supplier))) {
                        mSupplierName = ReadymixEntry.CEMEX_SUPPLIER;
                        mSupplierEmailEditText.setText(supplierEmailEditTextAdapter.getItem(position).toString());
                        mSupplierPhoneEditText.setText(supplierPhoneEditTextAdapter.getItem(position).toString());
                    } else if (selection.equals(getString(R.string.ubinto_supplier))) {
                        mSupplierName = ReadymixEntry.UBINTO_SUPPLIER;
                        mSupplierEmailEditText.setText(supplierEmailEditTextAdapter.getItem(position).toString());
                        mSupplierPhoneEditText.setText(supplierPhoneEditTextAdapter.getItem(position).toString());
                    } else {
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

    public void selectImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 2:
                if (resultCode == RESULT_OK) {
                    Uri choosenImage = data.getData();

                    if (choosenImage != null) {

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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Convert bitmap to bytes
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private byte[] imageToDB(Bitmap b) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 0, bos);
        return bos.toByteArray();

    }


    /**
     * Get user input from editor and save readymix concrete product into database.
     */
    private void saveReadymix() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        if (!(mBitmap == null)) {
            mPhoto = imageToDB(mBitmap);
        } else {
            mPhoto = null;
        }

        String supplierEmailString = mSupplierEmailEditText.getText().toString();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString();

        // Check if this is supposed to be a new readymix product
        // and check if all the fields in the EditProductActivity are blank
        if (mCurrentReadymixUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && mPhoto == null &&
                mSupplierName.equals(ReadymixEntry.OTHER_SUPPLIER) &&
                TextUtils.isEmpty(supplierEmailString) && TextUtils.isEmpty(supplierPhoneString) ) {
            // Since no fields were modified, we can return early without creating a new readymix product.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and readymix attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ReadymixEntry.COLUMN_READYMIX_NAME, nameString);
        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 1 by default.
        int price = 1;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(ReadymixEntry.COLUMN_READYMIX_PRICE, price);
        // If the quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 1 by default.
        int quantity = 1;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ReadymixEntry.COLUMN_READYMIX_QUANTITY, quantity);

        values.put(ReadymixEntry.COLUMN_PRODUCT_IMAGE, mPhoto);
        values.put(ReadymixEntry.COLUMN_READYMIX_NAME, mSupplierName);
        values.put(ReadymixEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailString);
        values.put(ReadymixEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneString);

        // Determine if this is a new or existing pet by checking if mCurrentPetUri is null or not
        if (mCurrentReadymixUri == null) {
            // This is a NEW pet, so insert a new readymix into the provider,
            // returning the content URI for the new readymix product.
            Uri newUri = getContentResolver().insert(ReadymixEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editProduct_insert_new_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editProduct_insert_new_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING readymix product, so update the readymix with content URI: mCurrentReadymixUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentReadymixUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentReadymixUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editProduct_update_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editProduct_update_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_edit_product.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_edit_product, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new readymix product, hide the "Delete" menu item.
        if (mCurrentReadymixUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save new readymix product to database
                saveReadymix();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the readymix product hasn't changed, continue with navigating up (back) to parent activity
                // which is {@link ViewProductActivity}.
                if (!mReadymixProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditProductActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditProductActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mReadymixProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the EditProductActivity shows all readymix product attributes, define a projection that contains
        // all columns from the readymix product table
        String[] projection = {
                ReadymixEntry._ID,
                ReadymixEntry.COLUMN_READYMIX_NAME,
                ReadymixEntry.COLUMN_READYMIX_PRICE,
                ReadymixEntry.COLUMN_READYMIX_QUANTITY,
                ReadymixEntry.COLUMN_SUPPLIER_NAME,
                ReadymixEntry.COLUMN_SUPPLIER_EMAIL,
                ReadymixEntry.COLUMN_SUPPLIER_PHONE,
                ReadymixEntry.COLUMN_PRODUCT_IMAGE};

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
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of readymix product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_READYMIX_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_READYMIX_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_READYMIX_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_PRODUCT_IMAGE);
            int supplierNameColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_SUPPLIER_EMAIL);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_SUPPLIER_PHONE);

            // Extract out the value from the Cursor for the given column index
            String readymixName = cursor.getString(nameColumnIndex);
            int readymixPrice = cursor.getInt(priceColumnIndex);
            int readymixQuantity = cursor.getInt(quantityColumnIndex);
            byte[] currentImage = cursor.getBlob(imageColumnIndex);
            String readymixSupplierName = cursor.getString(supplierNameColumnIndex);
            String readymixSupplierEmail = cursor.getString(supplierEmailColumnIndex);
            String readymixSupplierPhone = cursor.getString(supplierPhoneColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(readymixName);
            mPriceEditText.setText(readymixPrice);
            mQuantityEditText.setText(readymixQuantity);
            mSupplierEmailEditText.setText(readymixSupplierEmail);
            mSupplierPhoneEditText.setText(readymixSupplierPhone);
            if (!(currentImage == null)) {
                mProductImageView.setImageBitmap(convertToBitmap(currentImage));
            } else {
                mProductImageView.setImageResource(R.drawable.ic_action_add_image);
            }

            // readymixSupplierName is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Other Supplier, 1 is SRMCC, 2 is CEMEX and so on...).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (readymixSupplierName) {
                case ReadymixEntry.SRMCC_SUPPLIER:
                    mSupplierNameSpinner.setSelection(1);
                    break;
                case ReadymixEntry.CEMEX_SUPPLIER:
                    mSupplierNameSpinner.setSelection(2);
                    break;
                case ReadymixEntry.UBINTO_SUPPLIER:
                    mSupplierNameSpinner.setSelection(3);
                    break;
                case ReadymixEntry.BINLADEN_SUPPLIER:
                    mSupplierNameSpinner.setSelection(4);
                    break;
                default:
                    mSupplierNameSpinner.setSelection(0);

            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierNameSpinner.setSelection(0);
        mSupplierEmailEditText.setText("");
        mSupplierPhoneEditText.setText("");
        mProductImageView.setImageResource(R.drawable.ic_action_add_image);
    }

    //get bitmap image from currentImage byte array
    private Bitmap convertToBitmap(byte[] currentImage) {
        return BitmapFactory.decodeByteArray(currentImage, 0, currentImage.length);
    }



    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the Edit Product Activity.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the readymix product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }



    /**
     * Prompt the user to confirm that they want to delete this readymix product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the readymix product.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the readymix product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the readymix product in the database.
     */
    private void deletePet() {
        // Only perform the delete if this is an existing readymix product.
        if (mCurrentReadymixUri != null) {
            // Call the ContentResolver to delete the readymix product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentReadymixUri
            // content URI already identifies the readymix product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentReadymixUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editProduct_delete_readymix_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editProduct_delete_readymix_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

}
