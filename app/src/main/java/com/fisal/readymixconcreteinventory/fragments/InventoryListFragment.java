package com.fisal.readymixconcreteinventory.fragments;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fisal.readymixconcreteinventory.R;
import com.fisal.readymixconcreteinventory.ReadymixCursorAdapter;
import com.fisal.readymixconcreteinventory.data.ReadymixContract.ReadymixEntry;

public class InventoryListFragment extends Fragment implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the readymix product data loader */
    private static final int READYMIX_LOADER = 0;

    /** Adapter for the ListView */
    ReadymixCursorAdapter mCursorAdapter;

    private OnFragmentInteractionListener mListener;

    public InventoryListFragment() {
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
        View view = inflater.inflate(R.layout.fragment_inventory_list, container, false);

        // Find the ListView which will be populated with the readymix product data
        ListView readymixListView = (ListView) view.findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = view.findViewById(R.id.empty_view);
        readymixListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of readymix product data in the Cursor.
        // There is no readymix product data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ReadymixCursorAdapter(getContext(), null);
        readymixListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        readymixListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditProductActivity}
                Intent intent = new Intent(getContext(), com.fisal.readymixconcreteinventory.ViewProductDetailsActivity.class);

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

        return view;
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
        mListener = null;
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
        // String orderBy = ReadymixEntry.COLUMN_READYMIX_PRICE + " DESC";

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(getActivity(), // Parent activity context
                ReadymixEntry.CONTENT_URI,   // Provider content URI to query
                projection,                  // Columns to include in the resulting Cursor
                null,               // The columns for the WHERE clause - Selection criteria
                null,           // The values for the WHERE clause - Selection criteria
                null);                   // The sort order for the returned rows

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
