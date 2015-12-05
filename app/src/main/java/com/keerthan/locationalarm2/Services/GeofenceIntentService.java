package com.keerthan.locationalarm2.Services;

import android.app.IntentService;
import android.content.Intent;

public class GeofenceIntentService extends IntentService {

    private static final String TAG = "geofence-intent-service";
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * TAG Used to name the worker thread, important only for debugging.
     */
    public GeofenceIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
