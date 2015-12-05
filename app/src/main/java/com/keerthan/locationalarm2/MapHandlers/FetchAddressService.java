package com.keerthan.locationalarm2.MapHandlers;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.keerthan.locationalarm2.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class FetchAddressService extends IntentService {

    private static final String TAG = "fetch-address-service";
    private ResultReceiver receiver;

    public FetchAddressService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";
        receiver = intent.getParcelableExtra(Constants.RECEIVER);
        // Check if receiver was properly registered.
        if (receiver == null) {
            Log.wtf(TAG, "No receiver received. There is nowhere to send the results.");
            return;
        }

        LatLng location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
        if (location == null) {
            errorMessage = "No location data provided";
            Log.wtf(TAG, errorMessage);
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> list = null;
        try {
            list = geocoder.getFromLocation(location.latitude, location.longitude, 1);

        } catch (IOException e) {
            // Catch network or other I/O problems.
            Log.e(TAG, e.toString(), e);
        } catch (IllegalArgumentException e) {
            //if latitude and longitude values are invalid
            Log.e(TAG, e.toString(), e);
        }

        if(list==null || list.size()==0){
            errorMessage = "No addresses were found";
            Log.e(TAG, errorMessage);
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        }
        else{
            String address = "";      //this string will hold the address of the place selected by the user
            for (int i = 0; i < 4; i++) {
                if (list.iterator().next().getAddressLine(i) == null)
                    break;
                address = address + list.iterator().next().getAddressLine(i) + "\n";
            }
            deliverResultToReceiver(Constants.SUCCESS_RESULT, address);
        }
    }

    /**
     * Sends a resultCode and message to the receiver.
     */
    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        receiver.send(resultCode, bundle);
    }
}
