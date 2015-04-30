package com.example.sergio.quemedejes.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.example.sergio.quemedejes.provider.RouteContract;
import com.example.sergio.quemedejes.provider.RouteProvider;

import static com.example.sergio.quemedejes.provider.RouteProvider.ROUTE;

/*
    Uncomment this class when you are ready to test your UriMatcher.  Note that this class utilizes
    constants that are declared with package protection inside of the UriMatcher, which is why
    the test must be in the same data package as the Android app code.  Doing the test this way is
    a nice compromise between data hiding and testability.
 */
public class TestUriMatcher extends AndroidTestCase {
    private static final String LOCATION_QUERY = "London, UK";
    private static final long TEST_DATE = 1419033600L;  // December 20th, 2014
    private static final long TEST_LOCATION_ID = 10L;

    // content://com.example.android.sunshine.app/weather"
    private static final Uri TEST_ROUTE_DIR = RouteContract.RouteEntry.CONTENT_URI;
    private static final Uri TEST_ROUTE_WITH_LOCATION_DIR = RouteContract.RouteEntry.buildRouteLocation(LOCATION_QUERY);
    private static final Uri TEST_ROUTE_WITH_LOCATION_AND_DATE_DIR = RouteContract.RouteEntry.buildRouteLocationWithDate(LOCATION_QUERY, TEST_DATE);
    // content://com.example.android.sunshine.app/location"
    private static final Uri TEST_LOCATION_DIR = RouteContract.LocationEntry.CONTENT_URI;

    /*
        Students: This function tests that your UriMatcher returns the correct integer value
        for each of the Uri types that our ContentProvider can handle.  Uncomment this when you are
        ready to test your UriMatcher.
     */
    public void testUriMatcher() {
        UriMatcher testMatcher = RouteProvider.buildUriMatcher();

        assertEquals("Error: The ROUTE URI was matched incorrectly.",
                testMatcher.match(TEST_ROUTE_DIR),ROUTE);
        assertEquals("Error: The ROUTE WITH LOCATION URI was matched incorrectly.",
                testMatcher.match(TEST_ROUTE_WITH_LOCATION_DIR), RouteProvider.ROUTE_WITH_LOCATION);
        assertEquals("Error: The ROUTE WITH LOCATION AND DATE URI was matched incorrectly.",
                testMatcher.match(TEST_ROUTE_WITH_LOCATION_AND_DATE_DIR), RouteProvider.ROUTE_WITH_LOCATION_AND_DATE);
        assertEquals("Error: The LOCATION URI was matched incorrectly.",
                testMatcher.match(TEST_LOCATION_DIR), RouteProvider.LOCATION);
    }
}
