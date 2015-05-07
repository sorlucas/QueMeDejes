package com.example.sergio.quemedejes.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.sergio.quemedejes.provider.RouteContract;
import com.example.sergio.quemedejes.provider.RouteProvider;

/*
    Uncomment this class when you are ready to test your UriMatcher.  Note that this class utilizes
    constants that are declared with package protection inside of the UriMatcher, which is why
    the test must be in the same data package as the Android app code.  Doing the test this way is
    a nice compromise between data hiding and testability.
 */
public class TestUriMatcher extends AndroidTestCase {
    private static final long ROUTE_QUERY = 123;
    private static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    // content://com.example.android.sunshine.app/route"
    private static final Uri TEST_ROUTE_DIR = RouteContract.RouteEntry.CONTENT_URI;
    private static final Uri TEST_ROUTE_WITH_ID = RouteContract.RouteEntry.buildRouteUri(TEST_DATE);
    // content://com.example.android.sunshine.app/location"
    private static final Uri TEST_LOCATION_DIR = RouteContract.LocationEntry.CONTENT_URI;

    /*
        This function tests that your UriMatcher returns the correct integer value
        for each of the Uri types that our ContentProvider can handle.  */
    public void testUriMatcher() {

        Log.d("TestUriMatcher", "TEST_ROUTE_DIR" + TEST_ROUTE_DIR);
        Log.d("TestUriMatcher", "TEST_LOCATION_DIR" + TEST_ROUTE_WITH_ID);
        Log.d("TestUriMatcher", "TEST_LOCATION_DIR" + TEST_LOCATION_DIR);

        UriMatcher testMatcher = RouteProvider.buildUriMatcher();

        assertEquals("Error: The ROUTE URI was matched incorrectly.",
                testMatcher.match(TEST_ROUTE_DIR),RouteProvider.ROUTE);
        assertEquals("Error: The ROUTE URI was matched incorrectly.",
                testMatcher.match(TEST_ROUTE_WITH_ID),RouteProvider.ROUTE_WITH_ID);
        assertEquals("Error: The LOCATION URI was matched incorrectly.",
                testMatcher.match(TEST_LOCATION_DIR), RouteProvider.LOCATION);
    }
}
