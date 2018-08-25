package com.fossil.vn.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.fossil.vn.room.repository.RecordSessionRepository;

public class TrackingService extends Service {
    public static final String EVENT_EXTRA = "event_extra";
    public static final String ACTION_NEW_RECORD = "new_record";
    private LocationManager mLocationManager = null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        doSetUpTracking(intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mLocationManager != null) {
                mLocationManager.removeUpdates(LocationListener.getInstance(getApplicationContext()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void doSetUpTracking(Intent intent) {
        initializeLocationManager();

        if(!isReadyForTracking()) {
            // Todo broadcast event to UI know
            return;
        }
        boolean isNetworkProvider = false;
        boolean isGpsProvider = false;
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LocationListener.LOCATION_INTERVAL, LocationListener.LOCATION_DISTANCE,
                    LocationListener.getInstance(getApplicationContext()));
            isNetworkProvider = true;
        } catch (SecurityException | IllegalArgumentException ex) {
            //Todo broad cast Intent event to UI know request permission
            isNetworkProvider = false;
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LocationListener.LOCATION_INTERVAL, LocationListener.LOCATION_DISTANCE,
                    LocationListener.getInstance(getApplicationContext()));
            isGpsProvider = true;
        } catch (SecurityException | IllegalArgumentException ex) {
            isGpsProvider = false;
        }

        if (!isNetworkProvider && !isGpsProvider) {
            //Todo broad cast event to UI know request permission
            return;
        }

        if (intent != null) {
            String action = intent.getStringExtra(EVENT_EXTRA);
            if (action != null && action.equals(ACTION_NEW_RECORD)) {
                //Create new Record
                RecordSessionRepository recordSessionRepository = RecordSessionRepository.getInstance(getApplicationContext());
                recordSessionRepository.createNewRecord();
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private boolean isReadyForTracking() {
        boolean permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return permissionLocation;
    }
}
