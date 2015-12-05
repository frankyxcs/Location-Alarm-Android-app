package com.keerthan.locationalarm2.MapHandlers;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.keerthan.locationalarm2.Alarms.Alarm;
import com.keerthan.locationalarm2.MainActivity;
import com.keerthan.locationalarm2.R;

import java.util.List;


public class GeofenceTransitionIntentService extends IntentService {

    protected static final String TAG = "geofence-is";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * TAG Used to name the worker thread, important only for debugging.
     */
    public GeofenceTransitionIntentService() {
        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if(geofencingEvent.hasError()){
            String errorMessage = String.valueOf(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if(geofenceTransition== Geofence.GEOFENCE_TRANSITION_ENTER){
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            List<String> triggeringGeofencesIDs = getTriggeredGeofencesIds(triggeringGeofences);
            Alarm.triggerAlarms(triggeringGeofencesIDs, this);
        }
    }

    private List<String> getTriggeredGeofencesIds(List<Geofence> triggeringGeofences){
        List<String> IDs = null;
        for(Geofence geofence:triggeringGeofences){
            IDs.add(geofence.getRequestId());
        }
        return IDs;
    }

}
