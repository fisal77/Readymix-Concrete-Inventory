package com.fisal.readymixconcreteinventory;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.fisal.readymixconcreteinventory.data.ReadymixContract.ReadymixEntry;

/**
 * Allows user to view readymix concrete product details.
 */
public class ViewProductDetailsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the readymix product data loader
     */
    private static final int EXISTING_READYMIX_LOADER = 0;

    /**
     * Identifier for make phone call permission using supplier phone no. from database
     */
    private static final int MY_PERMISSION_FOR_PHONE_CALL = 1;

    /**
     * Content URI for the existing readymix product to view it's details.
     */
    private Uri mCurrentReadymixUri;

    /**
     * EditText field (Read Only - not editable) to view the readymix's name
     */
    private EditText mNameEditText;

    /**
     * EditText field (Read Only - not editable) to view the readymix's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field (Read Only - not editable) to view the readymix's quantity
     */
    private EditText mQuantityEditText;

    /**
     * ImageView field to view the readymix's product image
     */
    private ImageView mProductImageView;

    /**
     * Spinner (dropdown menu Read Only - not editable - not clickable) field to view the readymix's supplier name
     */
    private Spinner mSupplierNameSpinner;

    /**
     * EditText field (Read Only - not editable) to view the readymix's supplier email
     */
    private EditText mSupplierEmailEditText;

    /**
     * EditText field (Read Only - not editable) to view the readymix's supplier phone
     */
    private EditText mSupplierPhoneEditText;

    /**
     * Buttons to increase and decrease quantity
     */
    private Button decreaseQuantityBtn;
    private Button increaseQuantityBtn;

    /**
     * Image buttons for email and call the supplier data
     */
    private ImageButton sendEmailToSupplier;
    private ImageButton phoneCallToSupplier;

    /**
     * Supplier name of the readymix concrete. The possible valid values are in the ReadymixContract.java file:
     * {@link ReadymixEntry#OTHER_SUPPLIER}, {@link ReadymixEntry#SRMCC_SUPPLIER},
     * {@link ReadymixEntry#CEMEX_SUPPLIER}, {@link ReadymixEntry#UBINTO_SUPPLIER},
     * {@link ReadymixEntry#BINLADEN_SUPPLIER}.
     */
    private String mSupplierName = ReadymixEntry.OTHER_SUPPLIER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product_details);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're editing an existing readymix product by it's ID.
        Intent intent = getIntent();
        mCurrentReadymixUri = intent.getData();

        // Initialize a loader to read the readymix product data from the database
        // and display the current values to view the product details
        getLoaderManager().initLoader(EXISTING_READYMIX_LOADER, null, this);

        // Find all relevant views that we will need to view the product details
        mNameEditText = (EditText) findViewById(R.id.edit_readymix_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_readymix_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_readymix_quantity);
        mProductImageView = (ImageView) findViewById(R.id.edit_readymix_image);
        mSupplierNameSpinner = (Spinner) findViewById(R.id.spinner_supplier_name);
        mSupplierEmailEditText = (EditText) findViewById(R.id.edit_supplier_email);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.edit_supplier_phone);
        sendEmailToSupplier = (ImageButton) findViewById(R.id.emailImageButton);
        phoneCallToSupplier = (ImageButton) findViewById(R.id.phoneImageButton);
        increaseQuantityBtn = (Button) findViewById(R.id.increaseQuantity);
        decreaseQuantityBtn = (Button) findViewById(R.id.decreaseQuantity);

        increaseQuantityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mQuantityEditText.getText().toString().isEmpty()) {
                    mQuantityEditText.setText("0");
                }
                int quantityFromEditText = Integer.parseInt(mQuantityEditText.getText().toString());
                if (quantityFromEditText == 650) {
                    return;
                }
                if (quantityFromEditText >= 0) {
                    quantityFromEditText = quantityFromEditText + 1;
                    mQuantityEditText.setText(String.valueOf(quantityFromEditText));
                }
                if (quantityFromEditText < 0) {
                    quantityFromEditText = 1;
                    mQuantityEditText.setText(String.valueOf(quantityFromEditText));
                }
            }
        });

        decreaseQuantityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mQuantityEditText.getText().toString().isEmpty()) {
                    mQuantityEditText.setText("0");
                }
                int quantityFromEditText = Integer.parseInt(mQuantityEditText.getText().toString());
                if (quantityFromEditText == 0) {
                    return;
                }
                if (quantityFromEditText > 0) {
                    quantityFromEditText = quantityFromEditText - 1;
                    mQuantityEditText.setText(String.valueOf(quantityFromEditText));
                }
                if (quantityFromEditText < 1) {
                    quantityFromEditText = 0;
                    mQuantityEditText.setText(String.valueOf(quantityFromEditText));
                }
            }
        });

        setupSpinner();

        sendEmailToSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String supplierName = mSupplierNameSpinner.getSelectedItem().toString();
                String productName = mNameEditText.getText().toString();
                String supplierEmail = mSupplierEmailEditText.getText().toString();
                Intent sendEmail = new Intent(Intent.ACTION_SENDTO);
                sendEmail.setData(Uri.parse("mailto:"));
                sendEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{supplierEmail});
                sendEmail.putExtra(Intent.EXTRA_SUBJECT, supplierName + " - " + productName);
                if (sendEmail.resolveActivity(getPackageManager()) != null) {
                    startActivity(sendEmail);
                }
            }
        });

        phoneCallToSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(ViewProductDetailsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    // Check the user to give phone call permission.
                    getPermissionToMakePhoneCall();

                } else {
                    callSupplierPhone();
                }
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

        // Make the spinner as read only and not selectable.
        mSupplierNameSpinner.setEnabled(false);
        mSupplierNameSpinner.setClickable(false);
        mSupplierNameSpinner.setFocusable(false);
        mSupplierNameSpinner.setFocusableInTouchMode(false);

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_view_product_details.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_view_product_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Edit Product" menu option
            case R.id.action_edit:
                // Go to EditProductActivity to edit the existing readymix product in database
                editProductActivity();
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
                // Continue with navigating up (back) to parent activity
                // which is {@link InventoryListFragment}.
                    return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Start editProductActivity
     */
    private void editProductActivity() {
        Intent editProductIntent = new Intent(ViewProductDetailsActivity.this, EditProductActivity.class);
        // Set the URI on the data field of the intent
        editProductIntent.setData(mCurrentReadymixUri);
        startActivity(editProductIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the ViewProductDetailsActivity shows all readymix product attributes, define a projection that contains
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

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, // Parent activity context
                mCurrentReadymixUri,   // Provider content URI to query
                projection,                  // Columns to include in the resulting Cursor
                null,               // The columns for the WHERE clause - Selection criteria
                null,           // The values for the WHERE clause - Selection criteria
                null);                   // The sort order for the returned rows
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
            mPriceEditText.setText(Integer.toString(readymixPrice));
            mQuantityEditText.setText(Integer.toString(readymixQuantity));
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

    //get bitmap image from currentImage byte array (convert from byte as in SQLite database to bitmap ImageView)
    private Bitmap convertToBitmap(byte[] currentImage) {
        return BitmapFactory.decodeByteArray(currentImage, 0, currentImage.length);
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
                deleteProduct();
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
    private void deleteProduct() {
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


    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_FOR_PHONE_CALL) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "PHONE CALL permission granted", Toast.LENGTH_SHORT).show();
                // If permission granted by user then start phone call activity
                callSupplierPhone();
            } else {
                // showRationale = false if user clicks Never Ask Again, otherwise true
                boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE);

                if (showRationale) {
                    // do something here to handle degraded mode
                } else {
                    Toast.makeText(this, "PHONE CALL permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void callSupplierPhone() {
        String supplierPhone = mSupplierPhoneEditText.getText().toString();
        Intent phoneCall = new Intent(Intent.ACTION_CALL);
        phoneCall.setData(Uri.parse("tel:" + supplierPhone));
        phoneCall.putExtra(Intent.EXTRA_PHONE_NUMBER, supplierPhone);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            // Check the user to give phone call permission.
            getPermissionToMakePhoneCall();
        }
        startActivity(phoneCall);
    }

    @TargetApi(23)
    private void getPermissionToMakePhoneCall() {
        shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE);
        requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSION_FOR_PHONE_CALL);
    }

}
