package com.example.sergio.quemedejes.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.sergio.quemedejes.provider.RouteContract.LocationEntry;
import com.example.sergio.quemedejes.provider.RouteContract.RouteEntry;

/**
 * Manages a local database for route data.
 */
public class RouteDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    public static final String DATABASE_NAME = "route.db";

    public RouteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY," +
                LocationEntry.COLUMN_CITY_NAME_MEET + " TEXT UNIQUE NOT NULL, " +
                LocationEntry.COLUMN_CITY_NAME_INIT + " TEXT UNIQUE NOT NULL, " +
                LocationEntry.COLUMN_CITY_NAME_FINAL + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_COORD_LAT_INIT + " REAL NOT NULL, " +
                LocationEntry.COLUMN_COORD_LONG_INIT + " REAL NOT NULL, " +
                LocationEntry.COLUMN_COORD_LAT_FINAL + " REAL NOT NULL, " +
                LocationEntry.COLUMN_COORD_LONG_FINAL + " REAL NOT NULL, " +
                LocationEntry.COLUMN_COORD_LAT_MEET + " REAL NOT NULL, " +
                LocationEntry.COLUMN_COORD_LONG_MEET + " REAL NOT NULL " +
                " );";

        //bORRAR
        final String SQL_CREATE_ROUTE_TABLE = "CREATE TABLE " + RouteEntry.TABLE_NAME + " (" +

                RouteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RouteEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " + // the ID of the location entry associated with this weather data
                RouteEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                RouteEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +
                RouteEntry.COLUMN_DURATION_ROUTE + " REAL NOT NULL, " +
                RouteEntry.COLUMN_DISTANCE_ROUTE + " REAL NOT NULL, " +
                RouteEntry.COLUMN_IMG_URL + " TEXT NOT NULL, " +
                //RouteEntry.COLUMN_NAME_ROUTE + "TEXT NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + RouteEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "));";

                /*
                // To assure the application have just one weather entry per day
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + RouteEntry.COLUMN_DATE + ", " +
                RouteEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";
*/

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_ROUTE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RouteEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
