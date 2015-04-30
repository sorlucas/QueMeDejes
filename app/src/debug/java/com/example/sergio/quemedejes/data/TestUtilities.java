package com.example.sergio.quemedejes.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.example.sergio.quemedejes.provider.RouteContract;
import com.example.sergio.quemedejes.provider.RouteDbHelper;
import com.example.sergio.quemedejes.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * Created by sergio on 28/04/15.
 */
public class TestUtilities extends AndroidTestCase{

    static final String TEST_LOCATION_INIT = "Robledillo de gata";
    static final String TEST_LOCATION_FINAL = "Ciudad Rodrigo";
    static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        Students: Use this to create some default Route values for your database tests.
     */
    static ContentValues createRouteValues(long locationRowId) {

        // Create a new map of values, where column names are the keys
        ContentValues routeValues = new ContentValues();
        routeValues.put(RouteContract.RouteEntry.COLUMN_LOC_KEY, locationRowId);
        routeValues.put(RouteContract.RouteEntry.COLUMN_DATE, TEST_DATE);
        //routeValues.put(RouteContract.RouteEntry.COLUMN_ROUTE_NAME, "matalascañas");
        routeValues.put(RouteContract.RouteEntry.COLUMN_ROUTE_ID, "baja");
        routeValues.put(RouteContract.RouteEntry.COLUMN_DURATION_ROUTE, 40);
        routeValues.put(RouteContract.RouteEntry.COLUMN_DISTANCE_ROUTE, 180);

        return routeValues;

    }

    /*
        Students: You can uncomment this helper function once you have finished creating the
        LocationEntry part of the RouteContract.
     */
    static ContentValues createNorthPoleLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(RouteContract.LocationEntry.COLUMN_CITY_NAME_INIT, TEST_LOCATION_INIT);
        testValues.put(RouteContract.LocationEntry.COLUMN_CITY_NAME_FINAL, TEST_LOCATION_FINAL);
        testValues.put(RouteContract.LocationEntry.COLUMN_COORD_LAT_INIT, -147.353);
        testValues.put(RouteContract.LocationEntry.COLUMN_COORD_LONG_INIT, 64.7488);
        testValues.put(RouteContract.LocationEntry.COLUMN_COORD_LAT_FINAL, -146.353);
        testValues.put(RouteContract.LocationEntry.COLUMN_COORD_LONG_FINAL, 64.7488);

        return testValues;
    }

    /*
    Students: You can uncomment this function once you have finished creating the
    LocationEntry part of the RouteContract as well as the RouteDbHelper.
 */
    static long insertNorthPoleLocationValues(Context context) {
        // insert our test records into the database
        RouteDbHelper dbHelper = new RouteDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();

        long locationRowId;
        locationRowId = db.insert(RouteContract.LocationEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert North Pole Location Values", locationRowId != -1);

        return locationRowId;
    }

    /*
        Students: The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
