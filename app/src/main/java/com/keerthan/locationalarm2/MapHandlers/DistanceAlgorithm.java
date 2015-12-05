package com.keerthan.locationalarm2.MapHandlers;

import com.google.android.gms.maps.model.LatLng;

public class DistanceAlgorithm {

    public static final double R = 6372.8;                      //in kilometers

    //implements the Haversine formula to calculate distance
    public static double distance(LatLng pointA, LatLng pointB){
        double dLat = Math.toRadians(pointB.latitude - pointA.latitude);
        double dLon = Math.toRadians(pointB.longitude - pointA.longitude);
        double latA = Math.toRadians(pointA.latitude);
        double latB = Math.toRadians(pointB.latitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(latA) * Math.cos(latB);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }
}