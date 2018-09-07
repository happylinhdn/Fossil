package com.fossil.vn.service;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
    @Override
    public void onCreate() {
        super.onCreate();
    }

    private Constants.ServiceState state = Constants.ServiceState.IDL;

    private BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
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
            LocationListener.getInstance(getApplicationContext()).stopLocationUpdates();
            unregisterReceiver();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void startTrackLocationListener() {
        LocationListener.getInstance(getApplicationContext()).startLocationUpdates();
    }

    private void pauseTrackLocation() {
        LocationListener.getInstance(getApplicationContext()).stopLocationUpdates();
    }

    private void resumeTrackLocation() {
        startTrackLocationListener();
    }

    private void stopTrackService() {
        pauseTrackLocation();
        this.stopSelf();
    }

    private void doSetUpTracking() {
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

    private boolean isReadyForTracking() {
        boolean permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return permissionLocation;
    }
}
