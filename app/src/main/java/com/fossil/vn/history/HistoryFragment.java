package com.fossil.vn.history;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.fossil.vn.R;
import com.fossil.vn.common.BaseFragment;
import com.fossil.vn.common.TemplateActivity;
import com.fossil.vn.model.RecordModel;
import com.fossil.vn.record.RecordActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;


public class HistoryFragment extends BaseFragment implements TemplateActivity.FragmentBackListener {

    private View viwCurrent;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private GridLayoutManager mGridLayoutManager;
    private HistoryAdapter historyAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viwCurrent = inflater.inflate(R.layout.fragment_history, container, false);
        ((TemplateActivity) getActivity()).fragmentBackListener = this;
        mRecyclerView = viwCurrent.findViewById(R.id.recycler_view);
        mGridLayoutManager = new GridLayoutManager(getActivity(), 2);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());

        historyAdapter = new HistoryAdapter(getActivity());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(historyAdapter);
        mRecyclerView.setRecyclerListener(mRecycleListener);
        DividerItemDecoration itemDecor = new DividerItemDecoration(getActivity(), VERTICAL);
        mRecyclerView.addItemDecoration(itemDecor);

        View btnAddRecord = viwCurrent.findViewById(R.id.btn_new_record);
        btnAddRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ((HistoryActivity)getActivity()).openNewRecord();
                Intent intent = new Intent(getActivity(), RecordActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return viwCurrent;
    }

    private RecyclerView.RecyclerListener mRecycleListener = new RecyclerView.RecyclerListener() {

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            HistoryAdapter.ViewHolder mapHolder = (HistoryAdapter.ViewHolder) holder;
            if (mapHolder != null && mapHolder.map != null) {
                // Clear the map and free up resources by changing the map type to none.
                // Also reset the map when it gets reattached to layout, so the previous map would
                // not be displayed.
                mapHolder.map.clear();
                mapHolder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
            }
        }
    };


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void refreshUI() {
        ((HistoryActivity)getActivity()).setActionBarTitle(true, true, "History Record");
        ((HistoryActivity)getActivity()).hideMenuButton(true);
        initDataDemo();
    }

    @Override
    public boolean onFragmentBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage("Are you want to exit?")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                });
        builder.create().show();
        return true;
    }

    private void initDataDemo() {
        List<RecordModel> listData = new ArrayList<>();
        listData.add(new RecordModel(100, 10, 0L, 0L, new LatLng(-33.920455, 18.466941), new LatLng(-33.920455, 18.466941)));
        listData.add(new RecordModel(100, 10, 0L, 0L, new LatLng(10.7605174, 106.6968601), new LatLng(-33.920455, 18.466941)));
        listData.add(new RecordModel(100, 10, 0L, 0L, new LatLng(10.7605174, 106.6968601), new LatLng(-33.920455, 18.466941)));
        listData.add(new RecordModel(100, 10, 0L, 0L, new LatLng(10.7605174, 106.6968601), new LatLng(-33.920455, 18.466941)));
        listData.add(new RecordModel(100, 10, 0L, 0L, new LatLng(10.7605174, 106.6968601), new LatLng(-33.920455, 18.466941)));
        listData.add(new RecordModel(100, 10, 0L, 0L, new LatLng(10.7605174, 106.6968601), new LatLng(-33.920455, 18.466941)));
        listData.add(new RecordModel(100, 10, 0L, 0L, new LatLng(10.7605174, 106.6968601), new LatLng(-33.920455, 18.466941)));
        historyAdapter.initData(listData);
    }

}
