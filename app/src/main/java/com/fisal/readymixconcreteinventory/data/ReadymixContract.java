package com.fisal.readymixconcreteinventory.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Readymix Concrete Inventory app.
 */
public final class ReadymixContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private ReadymixContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.fisal.readymixconcreteinventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.fisal.readymixconcreteinventory/readymix/ is a valid path for
     * looking at pet data. content://com.fisal.readymixconcreteinventory/account/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "account".
     */
    public static final String PATH_READYMIX = "readymix";

    /**
     * Inner class that defines constant values for the Readymix database table.
     * Each entry in the table represents a single Readymix.
     */
    public static final class ReadymixEntry implements BaseColumns {

        /** The content URI to access the readymix data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_READYMIX);

        /** Name of database table for Readymix */
        public final static String TABLE_NAME = "readymix";

        /**
         * Unique ID number for the readymix (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Product Name of the readymix.
         *
         * Type: TEXT
         */
        public final static String COLUMN_READYMIX_NAME ="product_name";

        /**
         * Product price.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_READYMIX_PRICE = "price";

        /**
         * Product quantity.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_READYMIX_QUANTITY = "quantity";

        /**
         * Product image.
         *
         * Type: blob (binary)
         */
         public final static String COLUMN_PRODUCT_IMAGE = "image";

        /**
         * Supplier name.
         *
         * Type: TEXT
         */
        public final static String COLUMN_SUPPLIER_NAME ="supplier_name";

        /**
         * Supplier email.
         *
         * Type: TEXT
         */
        public final static String COLUMN_SUPPLIER_EMAIL ="supplier_email";

        /**
         * Supplier phone.
         *
         * Type: TEXT
         */
        public final static String COLUMN_SUPPLIER_PHONE ="supplier_phone";

        /**
         * Possible values for the supplier name of the readymix concrete.
         */
        public static final String OTHER_SUPPLIER = "Other Supplier";
        public static final String SRMCC_SUPPLIER = "Saudi Readymix Co.";
        public static final String CEMEX_SUPPLIER = "CEMEX";
        public static final String UBINTO_SUPPLIER = "Ubinto";
        public static final String BINLADEN_SUPPLIER = "Bin Laden Co.";

    }

}

