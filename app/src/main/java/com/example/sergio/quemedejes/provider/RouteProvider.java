package com.example.sergio.quemedejes.provider;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by sergio on 28/04/15.
 */
public class RouteProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RouteDbHelper mOpenHelper;

    public static final int ROUTE = 100;
    public static final int ROUTE_WITH_LOCATION = 101;
    public static final int ROUTE_WITH_LOCATION_AND_DATE = 102;
    public static final int LOCATION = 300;

    private static final SQLiteQueryBuilder sRouteByLocationSettingQueryBuilder;

    static{
        sRouteByLocationSettingQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sRouteByLocationSettingQueryBuilder.setTables(
                RouteContract.RouteEntry.TABLE_NAME + " INNER JOIN " +
                        RouteContract.LocationEntry.TABLE_NAME +
                        " ON " + RouteContract.RouteEntry.TABLE_NAME +
                        "." + RouteContract.RouteEntry.COLUMN_LOC_KEY +
                        " = " + RouteContract.LocationEntry.TABLE_NAME +
                        "." + RouteContract.LocationEntry._ID);
    }

    //location.location_setting = ?
    private static final String sLocationSettingSelection =
            RouteContract.LocationEntry.TABLE_NAME+
                    "." + RouteContract.LocationEntry.COLUMN_CITY_NAME_INIT + " = ? ";

    //location.location_setting = ? AND date >= ?
    private static final String sLocationSettingWithStartDateSelection =
            RouteContract.LocationEntry.TABLE_NAME+
                    "." + RouteContract.LocationEntry.COLUMN_CITY_NAME_INIT + " = ? AND " +
                    RouteContract.RouteEntry.COLUMN_DATE + " >= ? ";

    //location.location_setting = ? AND date = ?
    private static final String sLocationSettingAndDaySelection =
            RouteContract.LocationEntry.TABLE_NAME +
                    "." + RouteContract.LocationEntry.COLUMN_CITY_NAME_INIT + " = ? AND " +
                    RouteContract.RouteEntry.COLUMN_DATE + " = ? ";

    private Cursor getRouteByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = RouteContract.RouteEntry.getLocationSettingFromUri(uri);
        long startDate = RouteContract.RouteEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
            selection = sLocationSettingWithStartDateSelection;
        }

        return sRouteByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getRouteByLocationSettingAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String locationSetting = RouteContract.RouteEntry.getLocationSettingFromUri(uri);
        long date = RouteContract.RouteEntry.getDateFromUri(uri);

        return sRouteByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingAndDaySelection,
                new String[]{locationSetting, Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }

    /*
        Students: Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the ROUTE, ROUTE_WITH_LOCATION, ROUTE_WITH_LOCATION_AND_DATE,
        and LOCATION integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    public static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RouteContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, RouteContract.PATH_ROUTE, ROUTE);
        matcher.addURI(authority, RouteContract.PATH_ROUTE + "/*", ROUTE_WITH_LOCATION);
        matcher.addURI(authority, RouteContract.PATH_ROUTE + "/*/#", ROUTE_WITH_LOCATION_AND_DATE);

        matcher.addURI(authority, RouteContract.PATH_LOCATION, LOCATION);
        return matcher;
    }

    /*
        Students: We've coded this for you.  We just create a new RouteDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new RouteDbHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.

     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case ROUTE_WITH_LOCATION_AND_DATE:
                return RouteContract.RouteEntry.CONTENT_ITEM_TYPE;
            case ROUTE_WITH_LOCATION:
                return RouteContract.RouteEntry.CONTENT_TYPE;
            case ROUTE:
                return RouteContract.RouteEntry.CONTENT_TYPE;
            case LOCATION:
                return RouteContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "weather/*/*"
            case ROUTE_WITH_LOCATION_AND_DATE:
            {
                retCursor = getRouteByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            }
            // "weather/*"
            case ROUTE_WITH_LOCATION: {
                retCursor = getRouteByLocationSetting(uri, projection, sortOrder);
                break;
            }
            // "weather"
            case ROUTE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RouteContract.RouteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location"
            case LOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RouteContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case ROUTE: {
                normalizeDate(values);
                long _id = db.insert(RouteContract.RouteEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = RouteContract.RouteEntry.buildRouteUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LOCATION: {
                long _id = db.insert(RouteContract.LocationEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = RouteContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case ROUTE:
                rowsDeleted = db.delete(
                        RouteContract.RouteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LOCATION:
                rowsDeleted = db.delete(
                        RouteContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(RouteContract.RouteEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(RouteContract.RouteEntry.COLUMN_DATE);
            values.put(RouteContract.RouteEntry.COLUMN_DATE, RouteContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ROUTE:
                normalizeDate(values);
                rowsUpdated = db.update(RouteContract.RouteEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case LOCATION:
                rowsUpdated = db.update(RouteContract.LocationEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(RouteContract.RouteEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
