package com.example.sergio.quemedejes.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.example.sergio.quemedejes.provider.RouteContract;
import com.example.sergio.quemedejes.provider.RouteDbHelper;

/**
 * Created by sergio on 28/04/15.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(RouteDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createRouteValuesToday(System.currentTimeMillis() + 36000000)" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    public void testRouteTable() {
        // First insert the location, and then use the locationRowId to insert
        // the Route. Make sure to cover as many failure cases as you can.

        // Instead of rewriting all of the code we've already written in testLocationTable
        // we can move this code to insertLocation and then call insertLocation from both
        // tests. Why move it? We need the code to return the ID of the inserted location
        // and our testLocationTable can only return void because it's a test.


        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        RouteDbHelper dbHelper = new RouteDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step (Route): Create Route values
        ContentValues routeValues  = TestUtilities.createRouteValuesToday(System.currentTimeMillis() + 36000000);

        // Third Step (Route): Insert ContentValues into database and get a row ID back
        long routeRowId = db.insert(RouteContract.RouteEntry.TABLE_NAME, null, routeValues);
        assertTrue(routeRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor routeCursor = db.query(
                RouteContract.RouteEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue( "Error: No Records returned from location query", routeCursor.moveToFirst() );

        // Fifth Step: Validate the location Query
        TestUtilities.validateCurrentRecord("testInsertReadDb RouteEntry failed to validate",
                routeCursor, routeValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from Route query",
                routeCursor.moveToNext() );

        // Sixth Step: Close cursor and database
        routeCursor.close();
        dbHelper.close();
    }

}
