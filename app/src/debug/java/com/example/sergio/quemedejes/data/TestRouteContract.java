package com.example.sergio.quemedejes.data;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.example.sergio.quemedejes.provider.RouteContract;


public class TestRouteContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final String TEST_ROUTE_LOCATION = "/North Pole";
    private static final long TEST_ROUTE_DATE = 1419033600L;  // December 20th, 2014

    /*
        Students: Uncomment this out to test your ROUTE location function.
     */
    public void testBuildRouteLocation() {
        Uri locationUri = RouteContract.RouteEntry.buildRouteLocation(TEST_ROUTE_LOCATION);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildRouteLocation in " +
                        "RouteContract.",
                locationUri);
        assertEquals("Error: Route location not properly appended to the end of the Uri",
                TEST_ROUTE_LOCATION, locationUri.getLastPathSegment());
        assertEquals("Error: Route location Uri doesn't match our expected result",
                locationUri.toString(),
                "content://com.example.sergio.quemedejes/route/%2FNorth%20Pole");
    }
}
