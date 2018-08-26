package com.fossil.vn.record;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fossil.vn.R;
import com.fossil.vn.common.BaseFragment;
import com.fossil.vn.common.Constants;
import com.fossil.vn.common.Converter;
import com.fossil.vn.common.Node;
import com.fossil.vn.common.TemplateActivity;
import com.fossil.vn.common.Utils;
import com.fossil.vn.history.HistoryActivity;
import com.fossil.vn.room.entity.RecordSession;
import com.fossil.vn.room.repository.RecordSessionRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class RecordFragment extends BaseFragment implements TemplateActivity.FragmentBackListener, OnMapReadyCallback, RecordActivity.LoadDataEvent {
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
    RecordSession data;

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
        //Update listener
        ((RecordActivity)getActivity()).loadDataEvent = this;

        final RecordSessionRepository recordSessionRepository = RecordSessionRepository.getInstance(getActivity());
        recordSessionRepository.getLastRecord()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<RecordSession>() {
                    @Override
                    public void accept(RecordSession recordSession) throws Exception {
                        if (recordSession.isFinished()) {
                            initStateSession(Constants.ServiceState.STOP);
                        } else {
                            initStateSession(Constants.ServiceState.START);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    @Override
    public void onDestroy() {
        try {
            ((RecordActivity)getActivity()).loadDataEvent = null;
        } catch (Exception e) {

        }
        super.onDestroy();
    }

    @Override
    public boolean onFragmentBackPressed() {
        Intent intent = new Intent(getActivity(), HistoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().finish();
        return true;
    }

    private void initStateSession(Constants.ServiceState state) {
        switch (state) {
            case IDL:
                btnStart.setVisibility(View.GONE);
                btnPause.setVisibility(View.GONE);
                btnResume.setVisibility(View.GONE);
                btnStop.setVisibility(View.GONE);
                break;
            case START:
                btnStart.setVisibility(View.GONE);
                btnPause.setVisibility(View.VISIBLE);
                btnResume.setVisibility(View.GONE);
                btnStop.setVisibility(View.GONE);
                break;
            case PAUSE:
                btnStart.setVisibility(View.GONE);
                btnPause.setVisibility(View.GONE);
                btnResume.setVisibility(View.VISIBLE);
                btnStop.setVisibility(View.VISIBLE);
                break;
            case STOP:
                btnStart.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.GONE);
                btnResume.setVisibility(View.GONE);
                btnStop.setVisibility(View.GONE);
                break;
        }

    }

    private void startSession() {
        initStateSession(Constants.ServiceState.START);
        ((TemplateActivity)getActivity()).startRecord();
        ((RecordActivity)getActivity()).startTimeTrack();
    }

    private void pauseSession() {
        initStateSession(Constants.ServiceState.PAUSE);
        ((TemplateActivity)getActivity()).pauseRecord();
        ((RecordActivity)getActivity()).stopTimeTrack();
    }

    private void resumeSession() {
        initStateSession(Constants.ServiceState.START);
        ((TemplateActivity)getActivity()).resumeRecord();
        ((RecordActivity)getActivity()).startTimeTrack();
    }

    private void stopSession() {
        initStateSession(Constants.ServiceState.STOP);
        ((TemplateActivity)getActivity()).stopRecord();
        ((RecordActivity)getActivity()).stopTimeTrack();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        map = googleMap;
        setMapLocation();
    }

    private void setMapLocation() {
        if (map == null) return;
        map.clear();

//        RecordSession data = (RecordSession) mapView.getTag();
        if (data == null) return;
        if (data.getNodes().size() == 0) return;
        int sizeNode = data.getNodes().size();
        Node firstNode = data.getNodes().get(0);
        Node lastNode = data.getNodes().get(sizeNode - 1);
        map.addMarker(new MarkerOptions().position(firstNode.getLatLng()).title("Start Tour"));
        if (sizeNode > 1)
            map.addMarker(new MarkerOptions().position(lastNode.getLatLng()).title("End Tour"));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastNode.getLatLng(), Constants.CAMERA_MAP_ZOOM_LEVEL));
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        PolylineOptions polylineOptions = new PolylineOptions().width(5).color(Color.RED);
        float allDistance = 0;
        long allDuration = 0;
        float allSpeed = 0;
        double lastSpeed = 0;

        Node node = null;
        Node stNode = null;

        for (int i = 0; i < sizeNode; i++) {
            node = data.getNodes().get(i);
            polylineOptions.add(node.getLatLng());

            if (stNode != null) {
                // Distance in meter
                double distanceBetween = SphericalUtil.computeDistanceBetween(node.getLatLng(), stNode.getLatLng());
                //milliseconds
                long different = node.getTime().getTime() - stNode.getTime().getTime();
                long elapsedSeconds = different / Constants.SECONDS_IN_MIL;

                if (elapsedSeconds > 0) {
                    lastSpeed = distanceBetween / elapsedSeconds;
                    allSpeed += lastSpeed;
                }

                allDistance += distanceBetween;
                allDuration += elapsedSeconds;
            }

            stNode = node;
        }

        map.addPolyline(polylineOptions);

        // Calculate avg speed, duration
        float avgSpeed = 0;
        if (allDuration != 0 && sizeNode > 1) {
            avgSpeed = allDistance / allDuration;
//                avgSpeed = avgSpeed / (sizeNode - 1);
        }

        // Just show meter/second for demo
        tvDistance.setText(allDistance + " m");
        tvSpeed.setText(String.format("%.2f m/s", (float)lastSpeed));
        tvDuration.setText(Utils.getStringFromSecond(allDuration));
    }

    @Override
    public void needLoadData() {
        System.out.println("needLoadData");
        final RecordSessionRepository recordSessionRepository = RecordSessionRepository.getInstance(getActivity());
        recordSessionRepository.getLastRecord()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<RecordSession>() {
                    @Override
                    public void accept(RecordSession recordSession) throws Exception {
                        if (recordSession == null) {
                            recordSession = new RecordSession(Utils.getCurrent(), new ArrayList<Node>(), false);
                        }
                        data = recordSession;
                        mapView.setTag(recordSession);
                        setMapLocation();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    @Override
    public void updateTimeTick() {
        if (data != null) {
            Date cur = Utils.getCurrent();
            long different = cur.getTime() - data.getStartTime();
            System.out.println("updateTimeTick " + different);
            if (different  > 0) {
                long elapsedSeconds = different / Constants.SECONDS_IN_MIL;
                tvDuration.setText(Utils.getStringFromSecond(elapsedSeconds));
            }
        }
    }
}
