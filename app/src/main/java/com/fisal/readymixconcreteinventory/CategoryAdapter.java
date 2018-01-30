package com.fisal.readymixconcreteinventory;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.fisal.readymixconcreteinventory.fragments.AddInventoryFragment;
import com.fisal.readymixconcreteinventory.fragments.InventoryListFragment;

/**
 * Created by fisal on 28/01/2018.
 */


public class CategoryAdapter extends FragmentPagerAdapter {

    /** Context of the app */
    private Context mContext;

    public CategoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new InventoryListFragment();
        } else {
            return new AddInventoryFragment();
        }
    }

    /**
     * Return the total number of pages.
     */
    @Override
    public int getCount() {
        return 2;
    }

    /**
     * Return page title on the app bar based on the selected fragment position.
     */
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return mContext.getString(R.string.category_InventoryList);
        }  else {
            return mContext.getString(R.string.category_AddInventory);
        }
    }


}
