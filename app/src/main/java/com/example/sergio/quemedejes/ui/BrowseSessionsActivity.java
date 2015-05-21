/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.sergio.quemedejes.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.sergio.quemedejes.R;
import com.example.sergio.quemedejes.ui.widget.DrawShadowFrameLayout;

import static com.example.sergio.quemedejes.util.LogUtils.makeLogTag;

public class BrowseSessionsActivity extends BaseActivity  {
    private static final String TAG = makeLogTag(BrowseSessionsActivity.class);

    // How is this Activity being used?
    private static final int MODE_EXPLORE = 0; // as top-level "Explore" screen
    private static final int MODE_TIME_FIT = 1; // showing sessions that fit in a time interval

    private static final String STATE_FILTER_0 = "STATE_FILTER_0";
    private static final String STATE_FILTER_1 = "STATE_FILTER_1";
    private static final String STATE_FILTER_2 = "STATE_FILTER_2";

    public static final String EXTRA_FILTER_TAG = "com.google.android.iosched.extra.FILTER_TAG";

    private int mMode = MODE_EXPLORE;

    // filter tags that are currently selected
    private String[] mFilterTags = { "", "", "" };

    // filter tags that we have to restore (as a result of Activity recreation)
    private String[] mFilterTagsToRestore = { null, null, null };


    private DrawShadowFrameLayout mDrawShadowFrameLayout;
    private View mButterBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_browse_sessions);

        Toolbar toolbar = getActionBarToolbar();

        overridePendingTransition(0, 0);

        if (savedInstanceState != null) {
            mFilterTagsToRestore[0] = mFilterTags[0] = savedInstanceState.getString(STATE_FILTER_0);
            mFilterTagsToRestore[1] = mFilterTags[1] = savedInstanceState.getString(STATE_FILTER_1);
            mFilterTagsToRestore[2] = mFilterTags[2] = savedInstanceState.getString(STATE_FILTER_2);
        } else if (getIntent() != null && getIntent().hasExtra(EXTRA_FILTER_TAG)) {
            mFilterTagsToRestore[0] = getIntent().getStringExtra(EXTRA_FILTER_TAG);
        }

        if (mMode == MODE_EXPLORE) {
            // no title (to make more room for navigation and actions)
            // unless Nav Drawer opens
            toolbar.setTitle(null);
        }

        mButterBar = findViewById(R.id.butter_bar);
        mDrawShadowFrameLayout = (DrawShadowFrameLayout) findViewById(R.id.main_content);
        registerHideableHeaderView(mButterBar);

        //QuemedejesSyncAdapter.syncImmediately(getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        // we only have a nav drawer if we are in top-level Explore mode.
        return mMode == MODE_EXPLORE ? NAVDRAWER_ITEM_EXPLORE : NAVDRAWER_ITEM_INVALID;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        registerHideableHeaderView(findViewById(R.id.headerbar));
    }

    @Override
    protected void onActionBarAutoShowOrHide(boolean shown) {
        super.onActionBarAutoShowOrHide(shown);
        mDrawShadowFrameLayout.setShadowVisible(shown, shown);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.browse_sessions, menu);
        // remove actions when in time interval mode:
        if (mMode != MODE_EXPLORE) {
            menu.removeItem(R.id.menu_search);
            menu.removeItem(R.id.menu_refresh);
            menu.removeItem(R.id.menu_wifi);
            menu.removeItem(R.id.menu_debug);
            menu.removeItem(R.id.menu_about);
        } else {
            configureStandardMenuItems(menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_FILTER_0, mFilterTags[0]);
        outState.putString(STATE_FILTER_1, mFilterTags[1]);
        outState.putString(STATE_FILTER_2, mFilterTags[2]);
    }
}
