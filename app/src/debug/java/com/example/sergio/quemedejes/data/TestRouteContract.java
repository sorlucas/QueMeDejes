package com.example.sergio.quemedejes.data;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.example.sergio.quemedejes.provider.RouteContract;


public class TestRouteContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final long TEST_ROUTE_ID = 123;
    private static final long TEST_ROUTE_DATE = 1419033600L;  // December 20th, 2014

    public void testBuildRouteLocation() {
        Uri locationUri = RouteContract.RouteEntry.buildRouteUri(TEST_ROUTE_ID);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildRouteId in " +
                        "RouteContract.",
                locationUri);
        assertEquals("Error: Route location not properly appended to the end of the Uri",
                Long.toString(TEST_ROUTE_ID), locationUri.getLastPathSegment());
        assertEquals("Error: Route location Uri doesn't match our expected result",
                locationUri.toString(),
                "content://com.example.sergio.quemedejes/route/" + Long.toString(TEST_ROUTE_ID));
    }
}
