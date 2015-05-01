package com.example.sergio.quemedejes.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by sergio on 1/05/15.
 */
public class QuemedejesSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static QuemedejesSyncAdapter sQuemedejesSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("QuemedejesSyncService", "onCreate - QuemedejesSyncService");
        synchronized (sSyncAdapterLock) {
            if (sQuemedejesSyncAdapter == null) {
                sQuemedejesSyncAdapter = new QuemedejesSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sQuemedejesSyncAdapter.getSyncAdapterBinder();
    }
}
