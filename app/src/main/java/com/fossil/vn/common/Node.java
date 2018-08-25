package com.fossil.vn.common;

import android.location.Location;

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
}
