package com.fossil.vn.common;

import android.Manifest;

public class Constants {
    public static final String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static final String TIME_STAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    public static final float CAMERA_MAP_ZOOM_LEVEL = 18f;

    public static final long SECONDS_IN_MIL = 1000;
    public static final long MINUTES_IN_SECOND = 60;
    public static final long HOURS_IN_SECOND = 3600;

    public static final String DATA_UPDATE_EVENT = "com.fossil.vn.update_data";
    public static final String START_EVENT = "com.fossil.vn.start_event";
    public static final String PAUSE_EVENT = "com.fossil.vn.pause_event";
    public static final String RESUME_EVENT = "com.fossil.vn.resume_event";
    public static final String STOP_EVENT = "com.fossil.vn.stop_event";
    public enum ServiceState {
        IDL, READY, START, PAUSE, STOP
    }
}
