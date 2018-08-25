package com.fossil.vn.history;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fossil.vn.R;
import com.fossil.vn.model.RecordModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<RecordModel> listData;
    private Context context;

    public HistoryAdapter(Context context) {
        super();
        this.context = context;
        this.listData = new ArrayList<>();
    }

    public void initData(List<RecordModel> listData) {
        this.listData.clear();
        this.listData.addAll(listData);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false));
    }

    /**
     * This function is called when the user scrolls through the screen and a new item needs
     * to be shown. So we will need to bind the holder with the details of the next item.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder == null) {
            return;
        }
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

        MapView mapView;
        TextView tvSpeed;
        TextView tvDistance;
        TextView tvDuration;
        GoogleMap map;
        View layout;

        private ViewHolder(View itemView) {
            super(itemView);
            layout = itemView;
            mapView = layout.findViewById(R.id.lite_listrow_map);
            tvSpeed = layout.findViewById(R.id.tv_speed);
            tvDistance = layout.findViewById(R.id.tv_distance);
            tvDuration = layout.findViewById(R.id.tv_duration);
            if (mapView != null) {
                // Initialise the MapView
                mapView.onCreate(null);
                // Set the map ready callback to receive the GoogleMap object
                mapView.getMapAsync(this);
            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(context);
            map = googleMap;
            setMapLocation();
        }

        private void setMapLocation() {
            if (map == null) return;

            RecordModel data = (RecordModel) mapView.getTag();
            if (data == null) return;

            // Add a marker for this item and set the camera
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(data.getStartLocation(), 13f));
            map.addMarker(new MarkerOptions().position(data.getStartLocation()));

            // Set the map type back to normal.
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        private void bindView(int pos) {
            RecordModel item = listData.get(pos);
            // Store a reference of the ViewHolder object in the layout.
            layout.setTag(this);
            // Store a reference to the item in the mapView's tag. We use it to get the
            // coordinate of a location, when setting the map location.
            mapView.setTag(item);
            setMapLocation();
            tvDistance.setText(item.getDistance() + " km");
            tvSpeed.setText(item.getSpeed() + " km/h");
            tvDuration.setText("10:00:00");
        }
    }
}



