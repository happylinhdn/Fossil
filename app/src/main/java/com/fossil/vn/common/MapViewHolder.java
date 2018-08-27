package com.fossil.vn.common;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.fossil.vn.R;
import com.fossil.vn.room.entity.RecordSession;
import com.fossil.vn.service.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

public class MapViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {
    MapView mapView;
    TextView tvSpeed;
    TextView tvDistance;
    TextView tvDuration;
    GoogleMap map;
    View layout;
    Activity activity;
    MapLoader mapLoader;

    public MapViewHolder(Activity activity, MapLoader mapLoader, View itemView) {
        super(itemView);
        this.activity = activity;
        this.mapLoader = mapLoader;
        layout = itemView;
        mapView = layout.findViewById(R.id.lite_listrow_map);
        tvSpeed = layout.findViewById(R.id.tv_speed);
        tvDistance = layout.findViewById(R.id.tv_distance);
        tvDuration = layout.findViewById(R.id.tv_duration);
//        tvTest = layout.findViewById(R.id.tv_test);
        if (mapView != null) {
            // Initialise the MapView
            mapView.onCreate(null);
            // Set the map ready callback to receive the GoogleMap object
            mapView.getMapAsync(this);
        }
    }

    public MapView getMapView() {
        return mapView;
    }

    public TextView getTvSpeed() {
        return tvSpeed;
    }

    public TextView getTvDistance() {
        return tvDistance;
    }

    public TextView getTvDuration() {
        return tvDuration;
    }

    public GoogleMap getMap() {
        return map;
    }

    public View getLayout() {
        return layout;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(activity);
        map = googleMap;
        setMapLocation();
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
    }

    private void setMapLocation() {
        if (map == null) return;
        map.clear();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        Location currentLocation = LocationListener.getLastBestLocation(activity);
        if (currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, Constants.CAMERA_MAP_ZOOM_LEVEL));
        }

        RecordSession data = (RecordSession) mapView.getTag();
        if (data == null) return;
        if (data.getNodes().size() == 0) return;
        mapLoader.DisplayMap(data, this);
    }

    public void bindView(RecordSession item) {
        // Store a reference of the ViewHolder object in the layout.
        layout.setTag(this);
        // Store a reference to the item in the mapView's tag. We use it to get the
        // coordinate of a location, when setting the map location.
        mapView.setTag(item);
        setMapLocation();
        tvDistance.setText("- Km");
        tvSpeed.setText("- Km/h");
        tvDuration.setText("00:00:00");
    }

    public void updateDuration(long elapsedSeconds) {
        tvDuration.setText(Utils.getStringFromSecond(elapsedSeconds));

        RecordSession item = (RecordSession)mapView.getTag();
        if (item == null) {
            tvDistance.setText(String.format("%.2f Km", 0f));
            tvSpeed.setText(String.format("%.2f Km/h", 0f));
        } else {
            tvDistance.setText(String.format("%.2f Km", item.getAllDistanceInKm()));
            tvSpeed.setText(String.format("%.2f Km/h", item.getSpeedInKmh()));

        }
    }
}
