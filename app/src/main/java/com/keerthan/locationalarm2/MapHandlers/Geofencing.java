package com.keerthan.locationalarm2.MapHandlers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.keerthan.locationalarm2.Alarms.Alarm;

import java.util.LinkedList;

public class Geofencing implements ResultCallback<Status> {

    protected static final String TAG = "geofencing";
    private GeofencingRequest geofencingRequest;
    private GoogleApiClient mGoogleApiClient;
    protected LinkedList<Geofence> geofenceLinkedList;
    private Context context;

    public Geofencing(GoogleApiClient mGoogleApiClient, Context context) {
        this.mGoogleApiClient = mGoogleApiClient;
        this.context = context;
    }

    public Geofencing(GoogleApiClient mGoogleApiClient,Context context, LinkedList<Geofence> geofenceLinkedList) {
        this.geofenceLinkedList = geofenceLinkedList;
        this.context = context;
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public void PopulateandActivate(){
        populateGeofenceList();
        addGeofences();
    }
    protected void populateGeofenceList(){
        Log.i(TAG, "populateGeofenceList");

        geofenceLinkedList = null;
        LinkedList<Alarm> alarmLinkedList = Alarm.RetrieveList(context);

        if(alarmLinkedList != null) {
            for (Alarm alarm : alarmLinkedList) {
                geofenceLinkedList.add(new Geofence.Builder()
                                //sets request Id for this geofence.
                                //it is a String by which we can identify the geofence
                                .setRequestId(alarm.getName())

                                        //sets the circular region of the geofence
                                .setCircularRegion(alarm.getPosition().latitude,
                                        alarm.getPosition().longitude,
                                        alarm.getRange())

                                        //sets the expiration duration of the geofence.
                                        //by keeping it as NEVER_EXPIRE, the geofence is always present until programmatically removed
                                .setExpirationDuration(Geofence.NEVER_EXPIRE)

                                        //sets best effort notification responsiveness
                                .setNotificationResponsiveness(5)

                                        //sets the transition type of interest
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                                .build()
                );
            }
        }
        else{
            Log.e(TAG, "List is empty");
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        Log.i(TAG, "getGeofencingRequest");

        //builds a new geofencing request
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceLinkedList);
        return builder.build();
    }

    private PendingIntent getPendingIntent(){
        Intent intent = new Intent(context, GeofenceTransitionIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
        return PendingIntent.getService(context, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void addGeofences(){
        if(!mGoogleApiClient.isConnected()){
            Log.e(TAG, "addGeofences - client not connected");
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    //the google api client
                    mGoogleApiClient,
                    //the geofencing request object
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getPendingIntent()
            ).setResultCallback(this);

            Log.i(TAG, "added Geofences successfully");
        } catch (SecurityException e){
            //catch exception if app does not use ACCESS_FINE_LOCATION permission
            Log.e(TAG, "operation failed as ACCESS_FINE_LOCATION permission not granted");
            Toast.makeText(context, "Kindly grant ACCESS_FINE_LOCATION permission first and try again.", Toast.LENGTH_SHORT).show();
        } catch(IllegalArgumentException e){
            //when the request is empty
            //happens if no alarms are active
            Log.e(TAG, "Geofences were not added as no alarms are present");
        }
    }

    @Override
    public void onResult(Status status) {
        if(status.isSuccess()){
            Log.i(TAG, "Result - success");
        } else{
            Log.i(TAG, "Result - failure");
        }
    }
}
