package com.fossil.vn.common;

import com.fossil.vn.room.entity.RecordSession;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapCached {
    RecordSession recordSession;
    int id;
    MarkerOptions start;
    MarkerOptions end;
    CameraUpdate cameraLocation;
    PolylineOptions polylineOptions;
    float avgSpeed;
    float allDistance;
    long allDuration = 0;
    float allSpeed = 0;

    public float getAllDistanceInKm() {
        return allDistance/1000;
    }

    public float getAllDistance() {
        return allDistance;
    }

    public float getSpeedInKmh() {
        return avgSpeed * 3.6f;
    }

    public float getSpeed() {
        return avgSpeed;
    }
}
