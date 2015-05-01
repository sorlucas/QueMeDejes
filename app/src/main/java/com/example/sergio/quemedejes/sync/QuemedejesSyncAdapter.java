package com.example.sergio.quemedejes.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Log;

import com.example.sergio.quemedejes.R;
import com.example.sergio.quemedejes.provider.RouteContract;
import com.example.sergio.quemedejes.ui.BrowseSessionsActivity;
import com.example.sergio.quemedejes.util.Utility;

import java.util.Vector;

public class QuemedejesSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = QuemedejesSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 10;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;


    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[] {
            RouteContract.RouteEntry.COLUMN_DATE,
            RouteContract.RouteEntry.COLUMN_DURATION_ROUTE,
            RouteContract.RouteEntry.COLUMN_DISTANCE_ROUTE,
            RouteContract.RouteEntry.COLUMN_ROUTE_ID
    };

    // these indices must match the projection
    private static final int INDEX_COLUMN_DATE = 0;
    private static final int INDEX_DURATION_ROUTE = 1;
    private static final int INDEX_DISTANCE_ROUTE = 2;
    private static final int INDEX_ROUTE_ID = 3;

    public QuemedejesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        
        Log.d(LOG_TAG, "Starting sync");

        //TEST
        long TEST_DATE = 1419033600L;
        String city_init = "robledillo de gata";
        String city_final = "ciudad rodrigo";
        double lat_init = 147.353;
        double lon_init = 64.7488;
        double lat_final = 147.342;
        double lon_final = 63.7488;

        long locationId = addLocation(city_init,city_final,lat_init,lon_init,lat_final,lon_final);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.
        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.
        Time dayTime = new Time();
        dayTime.setToNow();
        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
        // now we work exclusively in UTC
        dayTime = new Time();

        
        ContentValues routeValues = new ContentValues();
        routeValues.put(RouteContract.RouteEntry.COLUMN_LOC_KEY, locationId);
        routeValues.put(RouteContract.RouteEntry.COLUMN_DATE, TEST_DATE);
        routeValues.put(RouteContract.RouteEntry.COLUMN_ROUTE_ID, "high");
        routeValues.put(RouteContract.RouteEntry.COLUMN_DURATION_ROUTE, 60);
        routeValues.put(RouteContract.RouteEntry.COLUMN_DISTANCE_ROUTE, 1800);

        // Insert the new route information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(routeValues.size());
        cVVector.add(routeValues);

        // add to database
        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContext().getContentResolver().bulkInsert(RouteContract.RouteEntry.CONTENT_URI, cvArray);

            // delete old data so we don't build up an endless history
            getContext().getContentResolver().delete(RouteContract.RouteEntry.CONTENT_URI,
                    RouteContract.RouteEntry.COLUMN_DATE + " <= ?",
                    new String[] {Long.toString(dayTime.setJulianDay(julianStartDay-1))});

            notifyRoute();
        }

        Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");
        
        return;
    }
    private void notifyRoute() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if ( displayNotifications ) {

            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                // Last sync was more than 1 day ago, let's send a notification with the weather.
                String locationQuery = Utility.getPreferredLocation(context);

                Uri weatherUri = RouteContract.RouteEntry.buildRouteLocationWithDate(locationQuery, System.currentTimeMillis());

                // we'll query our contentProvider, as always
                Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

                if (cursor.moveToFirst()) {
                    long date = cursor.getLong(INDEX_COLUMN_DATE);
                    double duration = cursor.getDouble(INDEX_DURATION_ROUTE);
                    double distance = cursor.getDouble(INDEX_DISTANCE_ROUTE);
                    String routeId = cursor.getString(INDEX_ROUTE_ID);

                    int iconId = Utility.getIconResourceForRouteCondition(routeId);
                    Resources resources = context.getResources();
                    Bitmap largeIcon = BitmapFactory.decodeResource(resources,
                            Utility.getArtResourceForRouteCondition(routeId));
                    String title = context.getString(R.string.app_name);

                    // Define the text of the forecast.
                    String contentText = String.format(context.getString(R.string.format_notification),
                            String.valueOf(date),
                            Utility.getFriendlyDayString(context, date),
                            String.valueOf(distance));

                    // NotificationCompatBuilder is a very convenient way to build backward-compatible
                    // notifications.  Just throw in some data.
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getContext())
                                    .setColor(resources.getColor(R.color.theme_primary))
                                    .setSmallIcon(iconId)
                                    .setLargeIcon(largeIcon)
                                    .setContentTitle(title)
                                    .setContentText(contentText);

                    // Make something interesting happen when the user clicks on the notification.
                    // In this case, opening the app is sufficient.
                    Intent resultIntent = new Intent(context, BrowseSessionsActivity.class);

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager =
                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
                    mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());

                    //refreshing last sync
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
                    editor.commit();
                }
                cursor.close();
            }
        }
    }
    /**
     * Helper method to handle insertion of a new location in the route database.
     *
     * @param cityNameInit The location string used to init route
     * @param cityNameFinal A human-readable city name, e.g "Mountain View"
     * @param lat_init the latitude of the city init
     * @param lon_init the longitude of the city init
     * @param lat_final the latitude of the city final
     * @param lon_final the longitude of the city final
     * @return the row ID of the added location.
     */
    long addLocation(String cityNameInit, String cityNameFinal, double lat_init, double lon_init,double lat_final, double lon_final) {
        long locationId;

        // First, check if the location with this city name exists in the db
        Cursor locationCursor = getContext().getContentResolver().query(
                RouteContract.LocationEntry.CONTENT_URI,
                new String[]{RouteContract.LocationEntry._ID},
                RouteContract.LocationEntry.COLUMN_CITY_NAME_INIT + " = ?",
                new String[]{cityNameInit},
                null);

        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(RouteContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues locationValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            locationValues.put(RouteContract.LocationEntry.COLUMN_CITY_NAME_INIT, cityNameInit);
            locationValues.put(RouteContract.LocationEntry.COLUMN_CITY_NAME_FINAL, cityNameFinal);
            locationValues.put(RouteContract.LocationEntry.COLUMN_COORD_LAT_INIT, lat_init);
            locationValues.put(RouteContract.LocationEntry.COLUMN_COORD_LONG_INIT, lon_init);
            locationValues.put(RouteContract.LocationEntry.COLUMN_COORD_LAT_FINAL, lat_final);
            locationValues.put(RouteContract.LocationEntry.COLUMN_COORD_LONG_FINAL, lon_final);

            // Finally, insert location data into the database.
            Uri insertedUri = getContext().getContentResolver().insert(
                    RouteContract.LocationEntry.CONTENT_URI,
                    locationValues
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            locationId = ContentUris.parseId(insertedUri);
        }

        locationCursor.close();
        // Wait, that worked?  Yes!
        return locationId;
    }


    //Metodos para configuracion de la Sync
    public static void initializeSyncAdapter(Context context) {
        Log.d("QuemedejesSyncAdapter", "initializeSyncAdapter");
        getSyncAccount(context);
    }
    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }
    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        QuemedejesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }
    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }
    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {

        Log.d("QuemedejesSyncAdapter", "syncImmediately");

        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

}