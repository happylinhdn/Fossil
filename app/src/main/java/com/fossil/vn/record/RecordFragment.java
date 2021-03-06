package com.fossil.vn.record;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fossil.vn.R;
import com.fossil.vn.common.BaseFragment;
import com.fossil.vn.common.Constants;
import com.fossil.vn.common.MapLoader;
import com.fossil.vn.common.MapViewHolder;
import com.fossil.vn.common.Node;
import com.fossil.vn.common.TemplateActivity;
import com.fossil.vn.common.Utils;
import com.fossil.vn.history.HistoryActivity;
import com.fossil.vn.room.entity.RecordSession;
import com.fossil.vn.room.repository.RecordSessionRepository;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class RecordFragment extends BaseFragment implements TemplateActivity.FragmentBackListener, RecordActivity.LoadDataEvent {
    private View viwCurrent;
//    GoogleMap map;
    View btnStart;
    View btnPause;
    View btnResume;
    View btnStop;
    RecordSession data;
    MapLoader mapLoader;
    MapViewHolder mapViewHolder;
    Constants.ServiceState currentState = Constants.ServiceState.IDL;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viwCurrent = inflater.inflate(R.layout.fragment_record, container, false);
        ((TemplateActivity) getActivity()).fragmentBackListener = this;

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
        mapLoader = new MapLoader(getActivity());
        mapViewHolder = new MapViewHolder(getActivity(), mapLoader, viwCurrent);

        return viwCurrent;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapViewHolder.onLowMemory();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapViewHolder.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapViewHolder.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapViewHolder.onDestroy();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void refreshUI() {
        ((RecordActivity)getActivity()).loadDataEvent = this;

        final RecordSessionRepository recordSessionRepository = RecordSessionRepository.getInstance(getActivity());
        recordSessionRepository.getLastRecord()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<RecordSession>() {
                    @Override
                    public void accept(RecordSession recordSession) {
                        if (recordSession.isFinished()) {
                            initStateSession(Constants.ServiceState.STOP);
                            ((RecordActivity)getActivity()).stopTimeTrack();
                        } else {
                            initStateSession(Constants.ServiceState.START);
                            ((RecordActivity)getActivity()).startTimeTrack();
                            ((RecordActivity)getActivity()).resumeRecord();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        initStateSession(Constants.ServiceState.STOP);
                        ((RecordActivity)getActivity()).stopTimeTrack();
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
        if (currentState != Constants.ServiceState.STOP) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setMessage("You are in tracking mode, please close it first")
                    .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing
                        }
                    });
            builder.create().show();
            return false;
        } else {
            Intent intent = new Intent(getActivity(), HistoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().finish();
            return true;
        }
    }

    private void initStateSession(Constants.ServiceState state) {
        currentState = state;
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
        ((RecordActivity)getActivity()).startTimeTrack();
        ((TemplateActivity)getActivity()).startRecord();
    }

    private void pauseSession() {
        initStateSession(Constants.ServiceState.PAUSE);
        ((RecordActivity)getActivity()).stopTimeTrack();
        ((TemplateActivity)getActivity()).pauseRecord();
    }

    private void resumeSession() {
        initStateSession(Constants.ServiceState.START);
        ((RecordActivity)getActivity()).startTimeTrack();
        ((TemplateActivity)getActivity()).resumeRecord();
    }

    private void stopSession() {
        initStateSession(Constants.ServiceState.STOP);
        ((RecordActivity)getActivity()).stopTimeTrack();
        ((TemplateActivity)getActivity()).stopRecord();
    }

    private void setMapLocation() {
        if (data == null) {
            mapLoader.DisplayMap(((TemplateActivity)getActivity()).lastRecord, mapViewHolder, true);
        } else {
            mapLoader.DisplayMap(data, mapViewHolder, true);
        }
    }

    @Override
    public void needLoadData() {
        final RecordSessionRepository recordSessionRepository = RecordSessionRepository.getInstance(getActivity());
        recordSessionRepository.getLastRecord()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<RecordSession>() {
                    @Override
                    public void accept(RecordSession recordSession) throws Exception {
                        if (recordSession == null) {
                            recordSession = new RecordSession(Utils.getCurrentTime(), new ArrayList<Node>(), false);
                        }
                        data = recordSession;
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapViewHolder.updateData(data);
                }
            });
        }
    }
}
