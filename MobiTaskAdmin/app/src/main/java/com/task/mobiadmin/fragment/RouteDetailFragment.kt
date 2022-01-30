package com.task.mobiadmin.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.task.mobiadmin.R
import com.task.mobiadmin.adapter.RouteDialogAdapter
import com.task.mobiadmin.data.RouteData
import com.task.mobiadmin.data.TaskData
import com.task.mobiadmin.utils.Utils
import kotlinx.android.synthetic.main.fg_route_detail.*
import java.util.*

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class RouteDetailFragment : BaseFragment() {

    private var vieww: View? = null

    private var taskData: TaskData? = null
    internal var datas: ArrayList<RouteData> = ArrayList()
    private var googleMap: GoogleMap? = null
    private var marker: Marker? = null
    private var dialog: AlertDialog? = null
    private var firstRun = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null && arguments.containsKey("data")) {
            taskData = arguments.getParcelable("data")
            datas = arguments.getParcelableArrayList("datas")
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbar(true, "Task Route")
        if (vieww != null) {
            val parent = vieww!!.parent as ViewGroup
            parent.removeView(vieww)
        }
        try {
            vieww = inflater!!.inflate(R.layout.fg_route_detail, null)
        } catch (e: InflateException) {
            e.printStackTrace()
        }
        return vieww
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeMap()
        markersIV.setOnClickListener { focusAllMarkers() }
        listIV.setOnClickListener { showRouteList() }
    }


    private fun initializeMap() {
        if (googleMap == null) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                googleMap.uiSettings.isMapToolbarEnabled = false
                this@RouteDetailFragment.googleMap = googleMap
                drawOnMap()
            }
        }
    }

    private fun drawOnMap() {

        for (i in 0 until datas.size - 1) {
            googleMap?.addPolyline(PolylineOptions()
                    .add(LatLng(datas[i].lat, datas[i].lng),
                            LatLng(datas[i + 1].lat, datas[i + 1].lng))
                    .color(Color.BLUE)
                    .width(4f)
                    .clickable(true))
        }
        googleMap?.addMarker(MarkerOptions()
                .position(LatLng(datas[0].lat, datas[0].lng))
                .title("Start Position")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        googleMap?.addMarker(MarkerOptions()
                .position(LatLng(datas[datas.size - 1].lat, datas[datas.size - 1].lng))
                .title("End Position")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
        focusAllMarkers()
    }

    private fun focusAllMarkers() {
        val builder = LatLngBounds.Builder()
        for (routeData in datas) {
            builder.include(LatLng(routeData.lat, routeData.lng))
        }
        if (firstRun) {
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50))
            firstRun = false
        } else {
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50))
        }
    }

    private fun showRouteList() {
        val builder = AlertDialog.Builder(baseActivity)
        val view = View.inflate(baseActivity, R.layout.dialog_route, null)
        val listRV = view.findViewById<View>(R.id.listRV) as RecyclerView
        listRV.layoutManager = LinearLayoutManager(baseActivity)
        listRV.adapter = RouteDialogAdapter(baseActivity, datas, this)
        builder.setView(view)
        builder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
        dialog = builder.create()
        dialog?.show()
    }

    fun onLocationClick(routeData: RouteData) {
        if (marker != null && marker!!.isVisible)
            marker?.remove()
        marker = googleMap?.addMarker(MarkerOptions()
                .position(LatLng(routeData.lat, routeData.lng))
                .title(Utils.getAddress(routeData.lat, routeData.lng, baseActivity))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(routeData.lat, routeData.lng), 14f))
        dialog?.dismiss()
    }

    fun focusMarker(routeData: RouteData) {
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(routeData.lat, routeData.lng), 14f))
        dialog?.dismiss()
    }
}
