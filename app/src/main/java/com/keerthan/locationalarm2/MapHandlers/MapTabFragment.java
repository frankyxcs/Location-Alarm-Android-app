package com.keerthan.locationalarm2.MapHandlers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.keerthan.locationalarm2.Alarms.Alarm;
import com.keerthan.locationalarm2.R;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
/*
* Class that handles the tab with the map on the home screen (linked to main activity via a fragment)
*/
public class MapTabFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static GoogleMap map;   //used to handle the map on screen
    private GoogleApiClient client; //used to connect to the Google Locations API to enable fetching real time user location data
    private LocationRequest mLocationRequest = null; //handles the location request
    private Location mCurrentLocation = null; //holds the latest available user's location
    private String searchText = null;//user input search terms are handled by this
    Geofencing geofencedAlarms = null;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000; //The desired interval for location updates. Inexact. Updates may be more or less frequent.
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2; //The fastest rate for active location updates. Exact. Updates will never be more frequent than this value.
    public static final String TAG = "MapFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.map_screen, container, false);
        map = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_maps)).getMap();

        buildGoogleApiClient();
        buildGoogleMap();

        geofencedAlarms = new Geofencing(client, getActivity());

        //createLocationRequest();
        //startLocationUpdates();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        client.connect();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");

        createLocationRequest();
        startLocationUpdates();
        geofencedAlarms.PopulateandActivate();

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(client);
        LatLng user_location = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(user_location, 16);
        map.moveCamera(update);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.d(TAG, "onConnectionSuspended");
        client.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }


    protected synchronized void buildGoogleApiClient() {
        if (client == null) {
            //setting up the client to connect to the necessary services
            client = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    protected void buildGoogleMap() {
        if (map != null) {
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setMyLocationEnabled(true);                             //allows user to locate oneself with a button appearing inside fragment
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setCompassEnabled(true);

            ShowAlarms();



        /*
            The user upon long clicking on the map, can set an alarm
            for the point he clicked. The function will also display the address
            of the point clicked, most of the time.
            Yet to figure out how to handle cases when the user, say, clicks on the Indian Ocean. App crashes. :/
         */
            map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    SetAlarmDialogConfirmation(latLng, null);
                }
            });
        }


        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                SetAlarmDialogConfirmation(marker.getPosition(), marker);
                return true;
            }
        });
    }



    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected boolean startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        if (client.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, mLocationRequest, new com.google.android.gms.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mCurrentLocation = location;
                    Toast.makeText(getActivity(), mCurrentLocation.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }
        return false;
    }

    public void receiveSearchText(String searchText) {
        this.searchText = searchText;
        try {
            SearchAlgorithm.searchMapForGivenLocation(getActivity(), searchText, map);
        } catch (IOException e) {
            Log.e(TAG, e.toString(), e);
        }

    }

    public void SetAlarmDialogConfirmation(final LatLng latLng, final Marker marker) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity()); //the user is first asked for confirmation
        alertDialogBuilder.setTitle("Set Alarm")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Alarm.setAlarm(getActivity(), latLng);
                        if (marker != null) {
                            map.clear();
                            ShowAlarms();
                        }

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        Geocoder geocoder = new Geocoder(getActivity());
        String message = null;

        if (marker != null) {
            message = "Would you like to set an alarm for \n" + marker.getTitle() + "\n";
        } else {
            message = "Would you like to set an alarm for \n";
        }


        try {
            List<Address> list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (list != null) {
                String address = "";      //this string will hold the address of the place selected by the user
                for (int i = 0; i < list.iterator().next().getMaxAddressLineIndex(); i++) {
                    address = address + list.iterator().next().getAddressLine(i) + "\n";
                }
                message = message + address + "\b ?";                    //\b to eliminate the extra \n
            } else
                throw new IOException("Found no address");//if geocoder is not able to find any address,
        } catch (IOException e) {
            e.printStackTrace();
                    /*When the geocoder is not able to find the address for
                        the selected location, or when the function fails
                        unexpectedly, a generic message is shown
                     */
            message = "Would you like to set an alarm here?";
        }
        alertDialogBuilder.setMessage(message);
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    /*
    *ShowAlarms() places all the alarms with its ranges on the map
    *and then tries to fit them all in display with the user's location
    */
    public void ShowAlarms() {
        Log.d(TAG, "ShowAlarms");
        if (map != null) {
            LinkedList<Alarm> list = Alarm.RetrieveList(getActivity().getApplicationContext());                  //holds the list of active alarms
            if (list != null) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Alarm alarm : list) {
                    builder.include(alarm.getPosition());                       //the bounds change automatically to include the position in it
                    map.addMarker(new MarkerOptions()                           //adds a marker at the alarm location
                            .title(alarm.getName())
                            .position(alarm.getPosition()));
                    map.addCircle(new CircleOptions()                           //adds a circle showing the range of alarm
                            .center(alarm.getPosition())
                            .radius(alarm.getRange())
                            .fillColor(0xff0000)                                //random color. must figure out a way for color to be different
                            .visible(true));
                }
                if (mCurrentLocation != null) {
                    builder.include(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                }
                LatLngBounds bounds = builder.build();
                CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 20);  //20 is the extra padding along with the bounds
                map.animateCamera(update);
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }


    //---------------------ONLY FOR LOGS------------------------------------

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }
}