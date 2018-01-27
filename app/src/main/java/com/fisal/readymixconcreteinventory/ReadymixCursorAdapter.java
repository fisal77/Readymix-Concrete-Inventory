package com.fisal.readymixconcreteinventory;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fisal.readymixconcreteinventory.data.ReadymixContract.ReadymixEntry;


/**
 * Created by fisal on 25/01/2018.
 */

/**
 * {@link ReadymixCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of readymix product data as its data source. This adapter knows
 * how to create list items for each row of readymix product data in the {@link Cursor}.
 */
class ReadymixCursorAdapter extends CursorAdapter {

    private int quantity;

    /**
     * Constructs a new {@link ReadymixCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ReadymixCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the readymix product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current readymix product can be set on the text_view_readymix_name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.text_view_readymix_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.text_view_readymix_price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.text_view_readymix_quantity);
        ImageView productImageView = (ImageView) view.findViewById(R.id.image_view_readymix);
        Button saleButton = (Button) view.findViewById(R.id.sale);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantityFromTextView = Integer.parseInt(quantityTextView.getText().toString());
                if (quantityFromTextView == 1) {
                    return;
                }
                if (quantityFromTextView > 1) {
                    quantityFromTextView = quantityFromTextView - 1;
                    quantityTextView.setText(String.valueOf(quantityFromTextView));
                }
                if (quantityFromTextView < 1) {
                    quantityFromTextView = 1;
                    quantityTextView.setText(String.valueOf(quantityFromTextView));
                }
            }
        });

        // Find the columns of readymix product attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_READYMIX_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_READYMIX_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_READYMIX_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndex(ReadymixEntry.COLUMN_PRODUCT_IMAGE);

        // Read the readymix product attributes from the Cursor for the current readymix product
        String readymixName = cursor.getString(nameColumnIndex);
        int readymixPrice = cursor.getInt(priceColumnIndex);
        int readymixQuantity = cursor.getInt(quantityColumnIndex);
        byte[] currentImage = cursor.getBlob(imageColumnIndex);

        // Update the TextViews with the attributes for the current readymix product
        nameTextView.setText(readymixName);
        priceTextView.setText(Integer.toString(readymixPrice));
        quantityTextView.setText(Integer.toString(readymixQuantity));
        if (!(currentImage == null)) {
            productImageView.setImageBitmap(convertToBitmap(currentImage));
        } else {
            productImageView.setImageResource(R.drawable.ic_action_add_image);
        }
    }

    //get bitmap image from currentImage byte array
    private Bitmap convertToBitmap(byte[] currentImage) {
        return BitmapFactory.decodeByteArray(currentImage, 0, currentImage.length);
    }
}
