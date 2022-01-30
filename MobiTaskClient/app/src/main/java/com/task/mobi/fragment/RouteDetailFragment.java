package com.task.mobi.fragment;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.task.mobi.R;
import com.task.mobi.adapter.RouteDialogAdapter;
import com.task.mobi.data.RouteData;
import com.task.mobi.data.TaskData;
import com.task.mobi.utils.Utils;

import java.util.ArrayList;

import butterknife.OnClick;

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
public class RouteDetailFragment extends BaseFragment {

    public View view;


    TaskData taskData;
    ArrayList<RouteData> datas = new ArrayList<>();
    private GoogleMap googleMap;
    private Marker marker;
    private AlertDialog dialog;
    private boolean firstRun = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("data")) {
            taskData = getArguments().getParcelable("data");
            datas = getArguments().getParcelableArrayList("datas");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setToolbar(true, "Route");
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fg_route_detail, null);
        } catch (InflateException e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeMap();
    }


    private void initializeMap() {
        if (googleMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    googleMap.getUiSettings().setMapToolbarEnabled(false);
                    RouteDetailFragment.this.googleMap = googleMap;
                    drawOnMap();
                }
            });
        }
    }

    private void drawOnMap() {

        for (int i = 0; i < datas.size() - 1; i++) {
            googleMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(datas.get(i).lat, datas.get(i).lng),
                            new LatLng(datas.get(i + 1).lat, datas.get(i + 1).lng))
                    .color(Color.BLUE)
                    .width(4)
                    .clickable(true));
        }
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(datas.get(0).lat, datas.get(0).lng))
                .title("Start Position")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(datas.get(datas.size() - 1).lat, datas.get(datas.size() - 1).lng))
                .title("End Position")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        focusAllMarkers();
    }

    @OnClick(R.id.markersIV)
    public void focusAllMarkers() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (RouteData routeData : datas) {
            builder.include(new LatLng(routeData.lat, routeData.lng));
        }
        if (firstRun) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
            firstRun = false;
        } else {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
        }
    }


    @OnClick(R.id.listIV)
    public void showRouteList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(baseActivity);
        View view = View.inflate(baseActivity, R.layout.dialog_route, null);
        RecyclerView listRV = (RecyclerView) view.findViewById(R.id.listRV);
        listRV.setLayoutManager(new LinearLayoutManager(baseActivity));
        listRV.setAdapter(new RouteDialogAdapter(baseActivity, datas, this));
        builder.setView(view);
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    public void onLocationClick(RouteData routeData) {
        if (marker != null && marker.isVisible())
            marker.remove();
        marker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(routeData.lat, routeData.lng))
                .title(Utils.getAddress(routeData.lat, routeData.lng, baseActivity))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(routeData.lat, routeData.lng), 14));
        dialog.dismiss();
    }

    public void focusMarker(RouteData routeData) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(routeData.lat, routeData.lng), 14));
        dialog.dismiss();
    }
}
