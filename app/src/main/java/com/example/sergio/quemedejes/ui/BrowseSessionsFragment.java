package com.example.sergio.quemedejes.ui;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.sergio.quemedejes.R;
import com.example.sergio.quemedejes.provider.RouteContract;
import com.example.sergio.quemedejes.sync.QuemedejesSyncAdapter;
import com.example.sergio.quemedejes.util.Utility;


/**
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 */
public class BrowseSessionsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = BrowseSessionsFragment.class.getSimpleName();
    private BrowseSessionsAdapter mBrowseSessionsAdapter;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mUseTodayLayout;

    private static final String SELECTED_KEY = "selected_position";

    private static final int FORECAST_LOADER = 0;
    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            RouteContract.RouteEntry.TABLE_NAME + "." + RouteContract.RouteEntry._ID,
            //RouteContract.RouteEntry.COLUMN_NAME_ROUTE,
            RouteContract.RouteEntry.COLUMN_DATE,
            RouteContract.RouteEntry.COLUMN_SHORT_DESC,
            RouteContract.RouteEntry.COLUMN_DURATION_ROUTE,
            RouteContract.RouteEntry.COLUMN_DISTANCE_ROUTE,
            RouteContract.RouteEntry.COLUMN_IMG_URL,

            RouteContract.RouteEntry.COLUMN_CITY_NAME_MEET,
            RouteContract.RouteEntry.COLUMN_CITY_NAME_INIT,
            RouteContract.RouteEntry.COLUMN_CITY_NAME_FINAL,
            RouteContract.RouteEntry.COLUMN_COORD_LAT_INIT,
            RouteContract.RouteEntry.COLUMN_COORD_LONG_INIT,
            RouteContract.RouteEntry.COLUMN_COORD_LAT_FINAL,
            RouteContract.RouteEntry.COLUMN_COORD_LONG_FINAL,
            RouteContract.RouteEntry.COLUMN_COORD_LAT_MEET,
            RouteContract.RouteEntry.COLUMN_COORD_LONG_MEET,
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_ROUTE_ID = 0;
    static final int COL_NAME_ROUTE = 1;
    static final int COL_ROUTE_DATE = 2;
    static final int COL_ROUTE_SHORT_DESC = 3;
    static final int COL_ROUTE_DURATION = 4;
    static final int COL_ROUTE_DISTANCE = 5;
    static final int COL_ROUTE_IMG_URL = 6;
    static final int COL_CITY_MEET = 7;
    static final int COL_CITY_INIT = 8;
    static final int COL_CITY_FINAL = 9;
    static final int COL_LAT_INIT = 10;
    static final int COL_LONG_INIT = 11;
    static final int COL_LAT_FINAL = 12;
    static final int COL_LONG_FINAL = 13;
    static final int COL_LAT_MEET = 14;
    static final int COL_LONG_MEET = 15;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    public BrowseSessionsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if (id == R.id.action_refresh) {
//            updateRoute();
//            return true;
//        }
        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The BrowseSessionsAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mBrowseSessionsAdapter = new BrowseSessionsAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_browse_sessions, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mBrowseSessionsAdapter);
        // We'll call our MainActivity
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity())
                            .onItemSelected(RouteContract.RouteEntry.buildRouteUri(cursor.getLong(COL_ROUTE_DATE)
                            ));
                }
                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        mBrowseSessionsAdapter.setUseTodayLayout(mUseTodayLayout);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    // since we read the location when we create the loader, all we need to do is restart things
    void onLocationChanged( ) {
        updateRoute();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    private void updateRoute() {
        QuemedejesSyncAdapter.syncImmediately(getActivity());
    }

    private void openPreferredLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if ( null != mBrowseSessionsAdapter ) {
            Cursor c = mBrowseSessionsAdapter.getCursor();
            if ( null != c ) {
                c.moveToPosition(0);
                String posLat = c.getString(COL_LAT_INIT);
                String posLong = c.getString(COL_LONG_INIT);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.

        // Sort order:  Ascending, by date.
        String sortOrder = RouteContract.RouteEntry.COLUMN_DATE + " ASC";

        Uri weatherForLocationUri = RouteContract.RouteEntry.CONTENT_URI;

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                RouteContract.RouteEntry.COLUMN_DATE,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBrowseSessionsAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBrowseSessionsAdapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mBrowseSessionsAdapter != null) {
            mBrowseSessionsAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }
}