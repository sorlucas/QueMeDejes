package com.example.sergio.quemedejes.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sergio.quemedejes.R;
import com.example.sergio.quemedejes.util.Utility;

/**
 * Created by sergio on 3/05/15.
 */
public class BrowseSessionsAdapter extends CursorAdapter {
    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout = true;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView distanceRoute;
        public final TextView durationRoute;
        public final TextView cityNameInit;
        public final TextView cityNameFinal;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            distanceRoute = (TextView) view.findViewById(R.id.list_item_high_textview);
            durationRoute = (TextView) view.findViewById(R.id.list_item_low_textview);
            cityNameInit = (TextView) view.findViewById(R.id.list_item_city_init_textview);
            cityNameFinal = (TextView) view.findViewById(R.id.list_item_city_final_textview);
        }
    }

    public BrowseSessionsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_forecast;         //FIXEDDDDD
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.list_item_forecast;
                break;
            }
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                // Get weather icon
                viewHolder.iconView.setImageResource(Utility.getArtResourceForRouteCondition(
                        cursor.getString(BrowseSessionsFragment.COL_ROUTE_DESC)));
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                // Get weather icon
                viewHolder.iconView.setImageResource(Utility.getIconResourceForRouteCondition(
                        cursor.getString(BrowseSessionsFragment.COL_ROUTE_DESC)));
                break;
            }
        }

        // Read date from cursor
        long dateInMillis = cursor.getLong(BrowseSessionsFragment.COL_ROUTE_DATE);
        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        // Read weather forecast from cursor
        String description = cursor.getString(BrowseSessionsFragment.COL_ROUTE_DESC);
        // Find TextView and set weather forecast on it
        viewHolder.descriptionView.setText(description);

        // For accessibility, add a content description to the icon field
        viewHolder.iconView.setContentDescription(description);

        // Read distance from cursor
        double distance = cursor.getDouble(BrowseSessionsFragment.COL_ROUTE_DISTANCE);
        viewHolder.distanceRoute.setText(String.valueOf(distance));

        // Read duration from cursor
        double duration = cursor.getDouble(BrowseSessionsFragment.COL_ROUTE_DURATION);
        viewHolder.durationRoute.setText(String.valueOf(duration));

        // Read city name init from cursor
        String cityNameInit = cursor.getString(BrowseSessionsFragment.COL_CITY_INIT);
        viewHolder.cityNameInit.setText(String.valueOf(cityNameInit));

        // Read city name final from cursor
        String cityNameFinal = cursor.getString(BrowseSessionsFragment.COL_CITY_FINAL);
        viewHolder.cityNameFinal.setText(String.valueOf(cityNameFinal));


    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}
