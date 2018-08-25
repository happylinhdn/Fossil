package com.fossil.vn.record;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fossil.vn.R;
import com.fossil.vn.common.BaseFragment;
import com.fossil.vn.common.TemplateActivity;
import com.fossil.vn.history.HistoryActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;


public class RecordFragment extends BaseFragment implements TemplateActivity.FragmentBackListener, OnMapReadyCallback {

    private View viwCurrent;
    MapView mapView;
    TextView tvSpeed;
    TextView tvDistance;
    TextView tvDuration;
    GoogleMap map;
    View btnStart;
    View btnPause;
    View btnResume;
    View btnStop;

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

        btnStart = viwCurrent.findViewById(R.id.btn_start);
        btnPause = viwCurrent.findViewById(R.id.btn_pause);
        btnResume = viwCurrent.findViewById(R.id.btn_resume);
        btnStop = viwCurrent.findViewById(R.id.btn_stop);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSession();
            }
        });
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseSession();
            }
        });
        btnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resumeSession();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSession();
            }
        });


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

    private void startSession() {
        btnStart.setVisibility(View.GONE);
        btnPause.setVisibility(View.VISIBLE);
        btnResume.setVisibility(View.GONE);
        btnStop.setVisibility(View.GONE);
        ((TemplateActivity)getActivity()).startRecord();
    }

    private void pauseSession() {
        btnStart.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        btnResume.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.VISIBLE);
        ((TemplateActivity)getActivity()).pauseRecord();
    }

    private void resumeSession() {
        btnStart.setVisibility(View.GONE);
        btnPause.setVisibility(View.VISIBLE);
        btnResume.setVisibility(View.GONE);
        btnStop.setVisibility(View.GONE);
        ((TemplateActivity)getActivity()).resumeRecord();
    }

    private void stopSession() {
        btnStart.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.GONE);
        btnResume.setVisibility(View.GONE);
        btnStop.setVisibility(View.GONE);
        ((TemplateActivity)getActivity()).startRecord();
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
