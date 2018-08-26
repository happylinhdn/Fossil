package com.fossil.vn.history;

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
import com.fossil.vn.common.Node;
import com.fossil.vn.common.Utils;
import com.fossil.vn.room.entity.RecordSession;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<RecordSession> listData;
    private Context context;

    public HistoryAdapter(Context context) {
        super();
        this.context = context;
        this.listData = new ArrayList<>();
    }

    public void initData(List<RecordSession> listData) {
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
        TextView tvTest;
        GoogleMap map;
        View layout;

        private ViewHolder(View itemView) {
            super(itemView);
            layout = itemView;
            mapView = layout.findViewById(R.id.lite_listrow_map);
            tvSpeed = layout.findViewById(R.id.tv_speed);
            tvDistance = layout.findViewById(R.id.tv_distance);
            tvDuration = layout.findViewById(R.id.tv_duration);
            tvTest = layout.findViewById(R.id.tv_test);
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
            map.clear();

            RecordSession data = (RecordSession) mapView.getTag();
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

            //Render history line
            PolylineOptions polylineOptions = new PolylineOptions().width(5).color(Color.RED);
            float allDistance = 0;
            long allDuration = 0;
            float allSpeed = 0;

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
                        double speed = distanceBetween / elapsedSeconds;
                        allSpeed += speed;
                    }

                    allDistance += distanceBetween;
                    allDuration += elapsedSeconds;
                }

                stNode = node;
            }

            map.addPolyline(polylineOptions);

            // calculate avg speed, duration
            float avgSpeed = 0;
            if (allDuration != 0 && sizeNode > 1) {
                avgSpeed = allDistance / allDuration;
//                avgSpeed = avgSpeed / (sizeNode - 1);
            }

            // Just show meter/second for demo
            tvDistance.setText(allDistance + " m");
            tvSpeed.setText(String.format("%.2f m/s", avgSpeed));
            tvDuration.setText(Utils.getStringFromSecond(allDuration));
        }

        private void bindView(int pos) {
            RecordSession item = listData.get(pos);
            // Store a reference of the ViewHolder object in the layout.
            layout.setTag(this);
            // Store a reference to the item in the mapView's tag. We use it to get the
            // coordinate of a location, when setting the map location.
            mapView.setTag(item);
            setMapLocation();
            tvDistance.setText("- m");
            tvSpeed.setText("- m/s");
            tvDuration.setText("00:00:00");
            tvTest.setText(Converter.fromDate(item.getStartTimeDate()));
        }
    }
}



