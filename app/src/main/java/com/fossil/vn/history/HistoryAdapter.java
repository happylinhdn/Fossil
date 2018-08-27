package com.fossil.vn.history;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fossil.vn.R;
import com.fossil.vn.common.Constants;
import com.fossil.vn.common.Converter;
import com.fossil.vn.common.MapLoader;
import com.fossil.vn.common.MapViewHolder;
import com.fossil.vn.common.Node;
import com.fossil.vn.common.Utils;
import com.fossil.vn.room.entity.RecordSession;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<MapViewHolder> {
    private List<RecordSession> listData;
    private Activity activity;
    private MapLoader mapLoader;

    public HistoryAdapter(Activity activity) {
        super();
        this.activity = activity;
        this.listData = new ArrayList<>();
        this.mapLoader = new MapLoader(activity);
    }

    public void initData(List<RecordSession> listData) {
        this.listData.clear();
        this.listData.addAll(listData);
        notifyDataSetChanged();
    }

    @Override
    public MapViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MapViewHolder(activity, mapLoader, LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false));
    }

    /**
     * This function is called when the user scrolls through the screen and a new item needs
     * to be shown. So we will need to bind the holder with the details of the next item.
     */
    @Override
    public void onBindViewHolder(MapViewHolder holder, int position) {
        if (holder == null) {
            return;
        }
        RecordSession item = listData.get(position);
        holder.bindView(item);
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public void cleanCached() {
        mapLoader.clearCache();
    }
}



