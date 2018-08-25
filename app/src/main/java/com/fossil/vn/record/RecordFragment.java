package com.fossil.vn.record;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fossil.vn.R;
import com.fossil.vn.common.BaseFragment;
import com.fossil.vn.common.TemplateActivity;
import com.fossil.vn.history.HistoryActivity;
import com.fossil.vn.model.RecordModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class RecordFragment extends BaseFragment implements TemplateActivity.FragmentBackListener, OnMapReadyCallback {

    private View viwCurrent;
    MapView mapView;
    TextView tvSpeed;
    TextView tvDistance;
    TextView tvDuration;
    GoogleMap map;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viwCurrent = inflater.inflate(R.layout.fragment_record, container, false);
        ((TemplateActivity) getActivity()).fragmentBackListener = this;

        mapView = viwCurrent.findViewById(R.id.lite_listrow_map);
        tvSpeed = viwCurrent.findViewById(R.id.tv_speed);
        tvDistance = viwCurrent.findViewById(R.id.tv_distance);
        tvDuration = viwCurrent.findViewById(R.id.tv_duration);
        if (mapView != null) {
            // Initialise the MapView
            mapView.onCreate(null);
            // Set the map ready callback to receive the GoogleMap object
            mapView.getMapAsync(this);
        }

        return viwCurrent;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void refreshUI() {
        ((TemplateActivity)getActivity()).setActionBarTitle(false, true, "Add Record");
        ((TemplateActivity)getActivity()).hideMenuButton(true);
    }

    @Override
    public boolean onFragmentBackPressed() {
        Intent intent = new Intent(getActivity(), HistoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().finish();
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        map = googleMap;
        setMapLocation();
    }

    private void setMapLocation() {
        //Todo: render map via database
        if (map == null) return;

//        RecordModel data = (RecordModel) mapView.getTag();
//        if (data == null) return;

        // Add a marker for this item and set the camera
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(10.7605174, 106.6968601), 18f));
//        map.addMarker(new MarkerOptions().position(data.getStartLocation()));

        // Set the map type back to normal.
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }
}
