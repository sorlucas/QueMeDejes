package com.example.sergio.quemedejes.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.sergio.quemedejes.provider.RouteContract.RouteEntry;

import java.util.ArrayList;

/**
 * Manages a local database for route data.
 */
public class RouteDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 4;

    public static final String DATABASE_NAME = "route.db";

    public RouteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_ROUTE_TABLE = "CREATE TABLE " + RouteEntry.TABLE_NAME + " (" +

                RouteEntry._ID + " INTEGER PRIMARY KEY NOT NULL," +

                //RouteEntry.COLUMN_NAME_ROUTE + "TEXT NOT NULL, " +
                RouteEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                RouteEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +
                RouteEntry.COLUMN_DURATION_ROUTE + " REAL NOT NULL, " +
                RouteEntry.COLUMN_DISTANCE_ROUTE + " REAL NOT NULL, " +
                RouteEntry.COLUMN_IMG_URL + " TEXT NOT NULL, " +
                RouteEntry.COLUMN_CITY_NAME_MEET + " TEXT NOT NULL, " +
                RouteEntry.COLUMN_CITY_NAME_INIT + " TEXT NOT NULL, " +
                RouteEntry.COLUMN_CITY_NAME_FINAL + " TEXT NOT NULL, " +
                RouteEntry.COLUMN_COORD_LAT_INIT + " REAL NOT NULL, " +
                RouteEntry.COLUMN_COORD_LONG_INIT + " REAL NOT NULL, " +
                RouteEntry.COLUMN_COORD_LAT_FINAL + " REAL NOT NULL, " +
                RouteEntry.COLUMN_COORD_LONG_FINAL + " REAL NOT NULL, " +
                RouteEntry.COLUMN_COORD_LAT_MEET + " REAL NOT NULL, " +
                RouteEntry.COLUMN_COORD_LONG_MEET + " REAL NOT NULL, " +

                // To assure the application have just one weather entry per day
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + RouteEntry._ID + ") ON CONFLICT REPLACE);";

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
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RouteEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }


    }
}
