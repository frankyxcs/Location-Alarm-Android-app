package com.keerthan.locationalarm2.MapHandlers;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class SearchAlgorithm {

    private static int MAX_NUMBER_OF_RESULTS = 15;
    private static int NO_RESULT_FOUND = 0;

    /*The Geocoder class is the one which has all important functions which can be used here
     * such as the getFromLocationName, which retrieves all the addresses corresponding to the location name
     * */

    protected static int placeAddressesOnMap(Geocoder geocoder, LatLngBounds mapBoundary, GoogleMap map,
                                      String locationName) throws IOException {


        List<Address> addressList = geocoder.getFromLocationName(locationName, MAX_NUMBER_OF_RESULTS, //holds a sequence of addresses
                mapBoundary.southwest.latitude, mapBoundary.southwest.longitude,
                mapBoundary.northeast.latitude, mapBoundary.northeast.longitude);


        if (addressList == null || addressList.size() == 0) {
            return NO_RESULT_FOUND;
        }

        int numberOfResults = 0;
        for (Address address : addressList) {
            numberOfResults++;
            //obtains the address and locates it on the map
            LatLng position = new LatLng(address.getLatitude(), address.getLongitude());
            map.addMarker(new MarkerOptions()
                    .position(position)
                    .title(address.getFeatureName()));
        }
        return numberOfResults;
    }

    /*this condition arises when the location is never found
    by the previous algorithm. The reason is simple,
    upon reaching minZoomLevel, the map still doesn't
    cover the entire world. So this method is invoked when the user
    wants to search for someplace across the world.

    The chief differences are the calling of the function getFromLocationName,
    and the moving of the camera in the loop
     */
    public static int placeAddressesOnMap(Geocoder geocoder, GoogleMap map,
                                          String locationName) throws IOException {

        List<Address> addressList = geocoder.getFromLocationName(locationName, MAX_NUMBER_OF_RESULTS);       //holds a sequence of addresses
        if (addressList == null || addressList.size() == 0) {
            return NO_RESULT_FOUND;
        }

        int numberOfResults = 0;
        for (Address address : addressList) {
            numberOfResults++;
            //obtains the address and locates it on the map
            LatLng position = new LatLng(address.getLatitude(), address.getLongitude());
            map.addMarker(new MarkerOptions()
                    .position(position)
                    .title(address.getFeatureName()));
            map.animateCamera(CameraUpdateFactory.newLatLng(position));
        }
        return numberOfResults;

    }

    //function to find all instances of the input search location term on the map and show it to the user
    public static void searchMapForGivenLocation(Context context,String locationName, GoogleMap map) throws IOException {
        if (!locationName.equals("")) {
            map.clear();                                                //so as to erase any previous markers placed on the map
            map.setMyLocationEnabled(true);
            Geocoder geocoder = new Geocoder(context);

            LatLngBounds mapBoundary = map.getProjection().getVisibleRegion().latLngBounds;
            boolean resultsFound = false;
            int numberOfResults = 0, zoomLevel = 15;
            final CameraPosition initialCameraPosition = map.getCameraPosition();
            final int MIN_ZOOM_LEVEL = (int) map.getMinZoomLevel();

            while (!resultsFound) {
                numberOfResults = placeAddressesOnMap(geocoder,mapBoundary, map, locationName);

                if (numberOfResults != NO_RESULT_FOUND) {
                    resultsFound = true;
                } else {
                    zoomLevel--;
                    if (zoomLevel == MIN_ZOOM_LEVEL) {
                        //search all over the world
                        numberOfResults = placeAddressesOnMap(geocoder, map, locationName);
                        break;
                    }
                    map.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
                    mapBoundary = map.getProjection().getVisibleRegion().latLngBounds;
                }
            }

            if (numberOfResults == NO_RESULT_FOUND) {
                Toast.makeText(context, "Couldn't find " + locationName, Toast.LENGTH_SHORT).show();
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(initialCameraPosition);
                map.moveCamera(update);
                return;
            } else if (numberOfResults < 5) {

                if(zoomLevel > MIN_ZOOM_LEVEL+1) {
                    map.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel-1));
                    mapBoundary = map.getProjection().getVisibleRegion().latLngBounds;
                    placeAddressesOnMap(geocoder, mapBoundary, map, locationName);
                }
                else{
                    placeAddressesOnMap(geocoder, map, locationName);
                }

            }

        }
    }
}
