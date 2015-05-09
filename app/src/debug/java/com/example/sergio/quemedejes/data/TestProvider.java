package com.example.sergio.quemedejes.data;


import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.example.sergio.quemedejes.provider.RouteContract;
import com.example.sergio.quemedejes.provider.RouteContract.RouteEntry;
import com.example.sergio.quemedejes.provider.RouteDbHelper;
import com.example.sergio.quemedejes.provider.RouteProvider;

/*
    Note: This is not a complete set of tests of the Sunshine ContentProvider, but it does test
    that at least the basic functionality has been implemented correctly.

    Students: Uncomment the tests in this class as you implement the functionality in your
    ContentProvider to make sure that you've implemented things reasonably correctly.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.

       Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
       the delete functionality in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                RouteEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                RouteEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Route table during delete", 0, cursor.getCount());
        cursor.close();

    }

    /*
        Student: Refactor this function to use the deleteAllRecordsFromProvider functionality once
        you have implemented delete functionality there.
     */
    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
        Students: Uncomment this test to make sure you've correctly registered the RouteProvider.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // RouteProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                RouteProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: RouteProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + RouteContract.CONTENT_AUTHORITY,
                    providerInfo.authority, RouteContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: RouteProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
            Students: Uncomment this test to verify that your implementation of GetType is
            functioning correctly.
         */
    public void testGetType() {
        // content://com.example.sergio.quemedejes/weather/
        String type = mContext.getContentResolver().getType(RouteEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.sergio.quemedejes/weather
        assertEquals("Error: the RouteEntry CONTENT_URI should return RouteEntry.CONTENT_TYPE",
                RouteEntry.CONTENT_TYPE, type);

        long testRouteId = 123;
        // content://com.example.sergio.quemedejes/weather/94074
        type = mContext.getContentResolver().getType(
                RouteEntry.buildRouteUri(testRouteId));
        // vnd.android.cursor.dir/com.example.sergio.quemedejes/weather
        assertEquals("Error: the RouteEntry CONTENT_URI with location should return RouteEntry.CONTENT_TYPE",
                RouteEntry.CONTENT_TYPE, type);

        long testDate = 1419120000L; // December 21st, 2014
        // content://com.example.sergio.quemedejes/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                RouteEntry.buildRouteUri(testRouteId).buildUpon().appendQueryParameter(RouteEntry.COLUMN_DATE,String.valueOf(testDate)).build());
        // vnd.android.cursor.item/com.example.sergio.quemedejes/weather/1419120000
        assertEquals("Error: the RouteEntry CONTENT_URI with location and date should return RouteEntry.CONTENT_ITEM_TYPE",
                RouteEntry.CONTENT_TYPE, type);

    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if the basic weather query functionality
        given in the ContentProvider is working correctly.
     */
    public void testBasicRouteQuery() {
        // insert our test records into the database
        RouteDbHelper dbHelper = new RouteDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues routeValues = TestUtilities.createRouteValuesToday();

        long weatherRowId = db.insert(RouteEntry.TABLE_NAME, null, routeValues);
        assertTrue("Unable to Insert RouteEntry into the Database", weatherRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor weatherCursor = mContext.getContentResolver().query(
                RouteEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertNotNull(weatherCursor);

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicRouteQuery", weatherCursor, routeValues);
    }

    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the insert functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testInsertReadProvider() {

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues routeValues = TestUtilities.createRouteValuesToday();
        // The TestContentObserver is a one-shot class
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(RouteEntry.CONTENT_URI, true, tco);
        Uri weatherInsertUri = mContext.getContentResolver().insert(RouteEntry.CONTENT_URI, routeValues);
        assertTrue(weatherInsertUri != null);

        long route_id = ContentUris.parseId(weatherInsertUri);

        // Did our content observer get called?  Students:  If this fails, your insert weather
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                RouteEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating RouteEntry insert.",
                weatherCursor, routeValues);

        // Get the joined Route and Location data
        weatherCursor = mContext.getContentResolver().query(
                RouteEntry.buildRouteUri(route_id),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Route and Location Data.",
                weatherCursor, routeValues);

        // Get the joined Route and Location data with a start date
        weatherCursor = mContext.getContentResolver().query(
                RouteEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                "ASC", // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Route and Location Data with start date.",
                weatherCursor, routeValues);

    }

    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the delete functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our weather delete.
        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(RouteEntry.CONTENT_URI, true, weatherObserver);

        deleteAllRecordsFromProvider();

        // Students: If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        weatherObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(weatherObserver);
    }

}