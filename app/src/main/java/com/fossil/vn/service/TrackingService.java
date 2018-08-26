package com.fossil.vn.service;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.fossil.vn.common.Constants;
import com.fossil.vn.common.Constants.ServiceState;
import com.fossil.vn.common.Node;
import com.fossil.vn.common.Utils;
import com.fossil.vn.room.entity.RecordSession;
import com.fossil.vn.room.repository.RecordSessionRepository;

import java.util.ArrayList;
import java.util.Date;

public class TrackingService extends Service {

    private Constants.ServiceState state = Constants.ServiceState.IDL;

    private LocationManager mLocationManager = null;
    private BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println("TrackingService " + action);
            if (action.equals(Constants.START_EVENT)) {
                state = ServiceState.START;
                //Create new Record
                RecordSessionRepository recordSessionRepository = RecordSessionRepository.getInstance(getApplicationContext());
                Date current = Utils.getCurrent();
                RecordSession recordSession = new RecordSession(current, new ArrayList<Node>(), false);
                recordSessionRepository.updateOrCreateRecord(recordSession);
                startTrackLocationListener();
            } else if (action.equals(Constants.PAUSE_EVENT)) {
                state = ServiceState.PAUSE;
                pauseTrackLocation();
            } else if (action.equals(Constants.RESUME_EVENT)) {
                state = ServiceState.START;
                resumeTrackLocation();
            } else if (action.equals(Constants.STOP_EVENT)) {
                state = ServiceState.STOP;
                stopTrackService();
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        doSetUpTracking();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mLocationManager != null) {
                mLocationManager.removeUpdates(LocationListener.getInstance(getApplicationContext()));
            }
            unregisterReceiver();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void startTrackLocationListener() {
        boolean isNetworkProvider = false;
        boolean isGpsProvider = false;
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LocationListener.LOCATION_INTERVAL, LocationListener.LOCATION_DISTANCE,
                    LocationListener.getInstance(getApplicationContext()));
            isNetworkProvider = true;
        } catch (SecurityException | IllegalArgumentException ex) {
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
    }

    private void pauseTrackLocation() {
        mLocationManager.removeUpdates(LocationListener.getInstance(getApplicationContext()));
        mLocationManager = null;
    }

    private void resumeTrackLocation() {
        startTrackLocationListener();
    }

    private void stopTrackService() {
        pauseTrackLocation();
        this.stopSelf();
    }

    private void doSetUpTracking() {
        initializeLocationManager();
        registerReceiver();
        if(!isReadyForTracking()) {
            // Todo broadcast event to UI know
            return;
        }
        state = ServiceState.READY;
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.START_EVENT);
        filter.addAction(Constants.PAUSE_EVENT);
        filter.addAction(Constants.RESUME_EVENT);
        filter.addAction(Constants.STOP_EVENT);
        LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(eventReceiver, filter);
    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(this.getApplicationContext()).unregisterReceiver(eventReceiver);
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
