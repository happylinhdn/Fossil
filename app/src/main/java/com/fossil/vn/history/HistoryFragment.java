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

import com.fossil.vn.R;
import com.fossil.vn.common.BaseFragment;
import com.fossil.vn.common.MapViewHolder;
import com.fossil.vn.common.TemplateActivity;
import com.fossil.vn.record.RecordActivity;
import com.fossil.vn.room.entity.RecordSession;
import com.fossil.vn.room.repository.RecordSessionRepository;
import com.google.android.gms.maps.GoogleMap;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.widget.LinearLayout.VERTICAL;


public class HistoryFragment extends BaseFragment implements TemplateActivity.FragmentBackListener {

    private View viwCurrent;
    private View llEmpty;
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
        llEmpty = viwCurrent.findViewById(R.id.ll_empty);
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
                Intent intent = new Intent(getActivity(), RecordActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
            }
        });
        showEmptyView();
        return viwCurrent;
    }

    private void showEmptyView() {
        if (historyAdapter != null && historyAdapter.getItemCount() > 0) {
            llEmpty.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            llEmpty.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    private RecyclerView.RecyclerListener mRecycleListener = new RecyclerView.RecyclerListener() {

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            MapViewHolder mapHolder = (MapViewHolder) holder;
            if (mapHolder != null && mapHolder.getMap() != null) {
                mapHolder.getMap().clear();
                mapHolder.getMap().setMapType(GoogleMap.MAP_TYPE_NONE);
            }
        }
    };


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (historyAdapter != null) {
            historyAdapter.cleanCached();
        }
    }

    @Override
    public void refreshUI() {
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
        return false;
    }

    private void initDataDemo() {
        RecordSessionRepository recordSessionRepository = RecordSessionRepository.getInstance(getActivity().getApplicationContext());
        recordSessionRepository.getAllRecord()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<RecordSession>>() {
                    @Override
                    public void accept(List<RecordSession> recordSessions) {
                        historyAdapter.initData(recordSessions);
                        showEmptyView();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                });

    }

}
