package com.fossil.vn.common;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class Node {
    double lat;
    double lng;
    float accuracy;
    String timeTrack;

    public Node(Location location, Date timeTrack) {
        this.lat = location.getLatitude();
        this.lng = location.getLongitude();
        this.accuracy = location.getAccuracy();
        this.timeTrack = Converter.fromDate(timeTrack);
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public String getTimeTrack() {
        return timeTrack;
    }

    public void setTimeTrack(String timeTrack) {
        this.timeTrack = timeTrack;
    }

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }

    public Date getTime() {
        return Converter.fromTimestamp(timeTrack);
    }
}
