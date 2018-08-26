package com.fossil.vn.service;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.fossil.vn.common.Constants;
import com.fossil.vn.common.Converter;
import com.fossil.vn.common.Node;
import com.fossil.vn.common.Utils;
import com.fossil.vn.room.entity.RecordSession;
import com.fossil.vn.room.repository.RecordSessionRepository;

import java.util.ArrayList;
import java.util.Calendar;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class LocationListener implements android.location.LocationListener {
    public static final int LOCATION_INTERVAL = 60000;
    public static final float LOCATION_DISTANCE = 10f;

    Context mContext;
    Location mLastLocation;
    private static LocationListener mInstance;

    public static LocationListener getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new LocationListener(context);
        }
        return mInstance;
    }

    private LocationListener(Context context) {
        mContext = context;
        mLastLocation = getLastBestLocation(context);
    }

    /**
     * @return the last know best location
     */
    private Location getLastBestLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        try {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            long GPSLocationTime = 0;
            if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

            long NetLocationTime = 0;

            if (null != locationNet) {
                NetLocationTime = locationNet.getTime();
            }

            if ( 0 < GPSLocationTime - NetLocationTime ) {
                return locationGPS;
            }
            else {
                return locationNet;
            }
        } catch (SecurityException e) {

        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        makeUseOfNewLocation(location);

        if(mLastLocation == null){
            mLastLocation = location;
        }

        if (mLastLocation != null) {
            double lat = mLastLocation.getLatitude();
            double lon = mLastLocation.getLongitude();
        }

        //Update database for current session record
        final RecordSessionRepository recordSessionRepository = RecordSessionRepository.getInstance(mContext);
        recordSessionRepository.getLastRecord()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<RecordSession>() {
                    @Override
                    public void accept(RecordSession recordSession) throws Exception {
                        if (recordSession == null) {
                            recordSession = new RecordSession(Utils.getCurrent(), new ArrayList<Node>(), false);
                        }
                        recordSession.getNodes().add(new Node(mLastLocation, Utils.getCurrent()));
                        recordSessionRepository.updateOrCreateRecord(recordSession);
                        System.out.println("LINH update: " + Converter.fromDate(recordSession.getStartTimeDate()));
                        LocalBroadcastManager.getInstance(mContext.getApplicationContext()).sendBroadcast(new Intent(Constants.DATA_UPDATE_EVENT));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });

    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public Location getLastLocation() {
        if (mLastLocation == null) {
//            mLastLocation  = new Gson().fromJson(Preference.getString(mContext, mContext.getResources().getString(
//                    R.string.shared_pref_location)), Location.class);
        }
        return mLastLocation;
    }

    /** Determines whether one location reading is better than the current location fix
     * @param location  The new location that you want to evaluate
     * @param currentBestLocation  The current location fix, to which you want to compare the new one.
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > LOCATION_INTERVAL;
        boolean isSignificantlyOlder = timeDelta < -LOCATION_INTERVAL;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location,
        // because the user has likely moved.
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse.
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /**
     * This method modify the last know good location according to the arguments.
     *
     * @param location The possible new location.
     */
    void makeUseOfNewLocation(Location location) {
        if ( isBetterLocation(location, mLastLocation) ) {
            mLastLocation = location;
        }
    }
}
