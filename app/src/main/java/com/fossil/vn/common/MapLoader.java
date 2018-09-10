package com.fossil.vn.common;

import android.app.Activity;
import android.graphics.Color;

import com.fossil.vn.room.entity.RecordSession;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapLoader {
    private MemoryCache memoryCache = new MemoryCache();
    private Map<MapViewHolder, Integer> mapviews = Collections.synchronizedMap(new WeakHashMap<MapViewHolder, Integer>());
    private ExecutorService executorService;
    private Activity activity;
    public MapLoader(Activity context) {
        activity = context;
        executorService = Executors.newFixedThreadPool(5);
    }

    public void DisplayMap(RecordSession recordSession, MapViewHolder mapHolder, boolean updateCached) {
        mapviews.put(mapHolder, recordSession.getId());
        MapCached mapCached = null;
        if (!updateCached) {
            mapCached = memoryCache.get(recordSession.getId());
        }
        if (mapCached != null){
            updateMapUI(mapHolder, mapCached);
        } else {
            queueMap(recordSession, mapHolder);
        }

    }

    public void DisplayMap(RecordSession recordSession, MapViewHolder mapHolder) {
        DisplayMap(recordSession, mapHolder, false);
    }

    private void updateMapUI(MapViewHolder mapHolder, MapCached mapCached) {
        GoogleMap map = mapHolder.getMap();
        map.clear();

        map.addMarker(mapCached.start);
        if (mapCached.end != null)
            map.addMarker(mapCached.end);

        map.moveCamera(mapCached.cameraLocation);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.addPolyline(mapCached.polylineOptions);


        if (mapCached.getAllDistanceInKm() > 0) {
            mapHolder.getTvDistance().setText(String.format("%,.2f km", mapCached.getAllDistanceInKm()));
        } else {
            mapHolder.getTvDistance().setText(String.format("%,.2f m", mapCached.getAllDistance()));
        }

        if (mapCached.getSpeedInKmh() > 0) {
            mapHolder.getTvSpeed().setText(String.format("%,.2f km/h", mapCached.getSpeedInKmh()));
        } else {
            mapHolder.getTvSpeed().setText(String.format("%,.2f m/s", mapCached.getSpeed()));
        }
        mapHolder.getTvDuration().setText(Utils.getStringFromSecond(mapCached.allDuration));
    }

    private void queueMap(RecordSession recordSession, MapViewHolder mapHolder) {
        CachedToLoad p = new CachedToLoad(recordSession, mapHolder);
        executorService.submit(new MapRunnable(p));
    }

    public static void caculateCached(RecordSession data) {
        if (data == null) return;
        if (data.getNodes().size() == 0) return;
        MapCached cached = new MapCached();
        cached.recordSession = data;
        int sizeNode = data.getNodes().size();
        Node firstNode = data.getNodes().get(0);
        Node lastNode = data.getNodes().get(sizeNode - 1);

        cached.start = new MarkerOptions().position(firstNode.getLatLng()).title("Start Tour");
        if (sizeNode > 1)
            cached.end = new MarkerOptions().position(lastNode.getLatLng()).title("End Tour");

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

//        cached.cameraLocation = CameraUpdateFactory.newLatLngZoom(lastNode.getLatLng(), Constants.CAMERA_MAP_ZOOM_LEVEL);

        //Render history line
        PolylineOptions polylineOptions = new PolylineOptions().width(10).color(Color.BLUE);

        float allDistance = 0;
        long allDuration = 0;
        float allSpeed = 0;

        Node node = null;
        Node stNode = null;

        for (int i = 0; i < sizeNode; i++) {
            node = data.getNodes().get(i);
            polylineOptions.add(node.getLatLng());
            builder.include(node.getLatLng());

            if (stNode != null) {
                // Distance in meter
                double distanceBetween = SphericalUtil.computeDistanceBetween(node.getLatLng(), stNode.getLatLng());
                //milliseconds
                long different = node.getTime().getTime() - stNode.getTime().getTime();
                long elapsedSeconds = different / Constants.SECONDS_IN_MIL;

                if (elapsedSeconds > 0) {
                    double speed = distanceBetween / elapsedSeconds;
                    allSpeed += speed;
                }

                allDistance += distanceBetween;
                allDuration += elapsedSeconds;
            }

            stNode = node;
        }
        LatLngBounds bounds = builder.build();
        cached.cameraLocation = CameraUpdateFactory.newLatLngBounds(bounds, 50);

        // calculate avg speed, duration
        float avgSpeed = 0;
        if (allDuration != 0 && sizeNode > 1) {
            avgSpeed = allDistance / allDuration;
        }

        cached.polylineOptions = polylineOptions;
        cached.allDistance = allDistance;
        cached.avgSpeed = avgSpeed;
        cached.allDuration = allDuration;

        data.setAllDistance(allDistance);
        data.setAllDuration(allDuration);
        data.setAvgSpeed(avgSpeed);
        data.setAllSpeed(allSpeed);
        data.cached = cached;
    }

    private class CachedToLoad {
        public RecordSession recordSession;
        public  MapViewHolder mapHolder;

        public CachedToLoad(RecordSession recordSession, MapViewHolder mapHolder) {
            this.recordSession = recordSession;
            this.mapHolder = mapHolder;
        }
    }

    class MapRunnable implements Runnable {
        CachedToLoad cachedToLoad;

        MapRunnable(CachedToLoad cachedToLoad) {
            this.cachedToLoad = cachedToLoad;
        }

        @Override
        public void run() {
            if (mapReused(cachedToLoad))
                return;
            caculateCached(cachedToLoad.recordSession);
            MapCached cached = cachedToLoad.recordSession.cached;
            memoryCache.put(cachedToLoad.recordSession.getId(), cached);
            if (mapReused(cachedToLoad))
                return;
            MapDisplayer bd = new MapDisplayer(cached, cachedToLoad);
            activity.runOnUiThread(bd);
        }
    }

    boolean mapReused(CachedToLoad cachedToLoad) {
        Integer idTag = mapviews.get(cachedToLoad.mapHolder);
        if (idTag == null || idTag != cachedToLoad.recordSession.getId())
            return true;
        return false;
    }

    class MapDisplayer implements Runnable {
        MapCached mapCached;
        CachedToLoad cachedToLoad;

        public MapDisplayer(MapCached mapCached, CachedToLoad p) {
            this.mapCached = mapCached;
            cachedToLoad = p;
        }

        public void run() {
            if (mapReused(cachedToLoad))
                return;
            if (mapCached != null){
                updateMapUI(cachedToLoad.mapHolder, mapCached);
            }
        }
    }

    public void clearCache() {
        memoryCache.clear();
        mapviews.clear();
    }
}
