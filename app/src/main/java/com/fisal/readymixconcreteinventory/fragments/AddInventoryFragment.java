package com.fisal.readymixconcreteinventory.fragments;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.fisal.readymixconcreteinventory.R;
import com.fisal.readymixconcreteinventory.data.ReadymixContract.ReadymixEntry;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddInventoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class AddInventoryFragment extends Fragment {

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
     * Buttons to increase and decrease quantity
     */
    private Button decreaseQuantityBtn;
    private Button increaseQuantityBtn;

    /**
     * Save button to insert into DB
     */
    private Button saveButton;

    /**
     * Supplier name of the readymix concrete. The possible valid values are in the ReadymixContract.java file:
     * {@link ReadymixEntry#OTHER_SUPPLIER}, {@link ReadymixEntry#SRMCC_SUPPLIER},
     * {@link ReadymixEntry#CEMEX_SUPPLIER}, {@link ReadymixEntry#UBINTO_SUPPLIER},
     * {@link ReadymixEntry#BINLADEN_SUPPLIER}.
     */
    private String mSupplierName = ReadymixEntry.OTHER_SUPPLIER;

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

    private OnFragmentInteractionListener mListener;

    public AddInventoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_inventory, container, false);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) view.findViewById(R.id.edit_readymix_name);
        mPriceEditText = (EditText) view.findViewById(R.id.edit_readymix_price);
        mQuantityEditText = (EditText) view.findViewById(R.id.edit_readymix_quantity);
        mProductImageView = (ImageView) view.findViewById(R.id.edit_readymix_image);
        mSupplierNameSpinner = (Spinner) view.findViewById(R.id.spinner_supplier_name);
        mSupplierEmailEditText = (EditText) view.findViewById(R.id.edit_supplier_email);
        mSupplierPhoneEditText = (EditText) view.findViewById(R.id.edit_supplier_phone);
        increaseQuantityBtn = (Button) view.findViewById(R.id.increaseQuantity);
        decreaseQuantityBtn = (Button) view.findViewById(R.id.decreaseQuantity);
        saveButton = (Button) view.findViewById(R.id.action_save_button);

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

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveReadymix();
            }
        });

        return view;
    }

    /**
     * Setup the dropdown spinner that allows the user to select the supplier details of the readymix.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter supplierNameSpinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.array_supplier_name_options, android.R.layout.simple_spinner_item);

        // Array adapters for supplier's email and phone. All both linked to the main supplier's name array adapter.
        // When user select the main array the others will be changed as same order.
        final ArrayAdapter supplierEmailEditTextAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.array_supplier_email_options, android.R.layout.simple_list_item_1);
        final ArrayAdapter supplierPhoneEditTextAdapter = ArrayAdapter.createFromResource(getContext(),
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(selectedImage), null, o);

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
            return BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(selectedImage), null, o2);
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
        if (mQuantityEditText.getText().equals("")) {
            mQuantityEditText.setText("0");
        }

        if (!(mBitmap == null)) {
            mPhoto = imageToDB(mBitmap);
        } else {
            mPhoto = null;
        }

        String supplierNameString = mSupplierNameSpinner.getSelectedItem().toString();
        String supplierEmailString = mSupplierEmailEditText.getText().toString();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString();

        // Check if this is supposed to be a new readymix product
        // and check if all the fields in the AddInventoryFragment are blank
        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) || mPhoto == null ||
                supplierNameString.equals(ReadymixEntry.OTHER_SUPPLIER) ||
                supplierEmailString.equals("N/A") || supplierPhoneString.equals("N/A")) {
            // Since no fields were modified, we can return early without creating a new readymix product.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            // Then display toast message that warn user.
            Toast.makeText(getContext(), getString(R.string.updateProduct_insert_blank_fields_warning),
                    Toast.LENGTH_LONG).show();
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
        values.put(ReadymixEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(ReadymixEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailString);
        values.put(ReadymixEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneString);


            // This is a NEW product, so insert a new readymix into the provider,
            // returning the content URI for the new readymix product.
            Uri newUri = getContext().getContentResolver().insert(ReadymixEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(getContext(), getString(R.string.editProduct_insert_new_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(getContext(), getString(R.string.editProduct_insert_new_successful),
                        Toast.LENGTH_SHORT).show();
            }
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
                getActivity().finish();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the readymix product hasn't changed, continue with navigating up (back) to parent activity
                // which is {@link InventoryListFragment}.
                if (!mReadymixProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(getActivity());
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
                                NavUtils.navigateUpFromSameTask(getActivity());
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
    public void onBackPressed() {
        // If the readymix product hasn't entered/changed, continue with handling back button press
        if (!mReadymixProductHasChanged) {
             super.getActivity().onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        getActivity().finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
        onBackPressed();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
