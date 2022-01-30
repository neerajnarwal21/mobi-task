package com.task.mobiadmin.fragment.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.task.mobiadmin.R
import com.task.mobiadmin.activity.BaseActivity
import com.task.mobiadmin.data.RouteData
import com.task.mobiadmin.fragment.RouteDetailFragment
import com.task.mobiadmin.retrofitManager.ResponseListener
import com.task.mobiadmin.utils.Const
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import java.util.*

/**
 * Created by neeraj on 28/5/18.
 */
class MapPathLoad(var frag: TaskDetailFragment, baseActivity: BaseActivity, var clickV: View, var emptyTV: TextView, var taskTimePB: ProgressBar) {
    private var pathCall: Call<JsonObject>? = null
    private var datas = ArrayList<RouteData>()

    init {
        loadPath()
        clickV.setOnClickListener {
            if (datas.size > 0) {
                val fg = RouteDetailFragment()
                val bundle = Bundle()
                bundle.putParcelable("data", frag.taskData)
                bundle.putParcelableArrayList("datas", datas)
                fg.arguments = bundle
                baseActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_container, fg)
                        .addToBackStack(null)
                        .commit()
            }
        }
    }

    private fun loadPath() {
        val sessId = RequestBody.create(MediaType.parse("text/plain"), frag.store.getString(Const.SESSION_ID))
        val taskId = RequestBody.create(MediaType.parse("text/plain"), frag.taskData!!.id)
        pathCall = frag.apiInterface.getTaskRoute(sessId, taskId)
        taskTimePB.visibility = View.VISIBLE
        frag.apiManager.makeApiCall(pathCall, false, object : ResponseListener {
            override fun onSuccess(call: Call<*>?, `object`: Any?) {
                if (pathCall === call) {
                    val jsonObject = `object` as JsonObject
                    val data = jsonObject.getAsJsonArray("Data")

                    val objectType = object : TypeToken<ArrayList<RouteData>>() {}.type
                    datas = Gson().fromJson<ArrayList<RouteData>>(data, objectType)
                    if (datas.size > 1) {
                        for (i in 0 until datas.size - 1) {
                            frag.googleMap.addPolyline(PolylineOptions()
                                    .add(LatLng(datas[i].lat, datas[i].lng),
                                            LatLng(datas[i + 1].lat, datas[i + 1].lng))
                                    .color(Color.BLUE)
                                    .width(4f)
                                    .clickable(true))
                        }
                        frag.googleMap.addMarker(MarkerOptions()
                                .position(LatLng(datas[0].lat, datas[0].lng))
                                .title("Start Position")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                        frag.googleMap.addMarker(MarkerOptions()
                                .position(LatLng(datas[datas.size - 1].lat, datas[datas.size - 1].lng))
                                .title("End Position")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
                        val builder = LatLngBounds.Builder()
                        for (routeData in datas)
                            builder.include(LatLng(routeData.lat, routeData.lng))
                        frag.googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 70))
                    }
                    frag.updateText(datas)
                    taskTimePB.visibility = View.GONE
                    emptyTV.visibility = View.GONE
                }
            }

            override fun onError(call: Call<*>?, statusCode: Int, errorMessage: String?, responseListener: ResponseListener?) {
                taskTimePB.visibility = View.GONE
                emptyTV.text = "No Timeline to this task"
            }
        })
    }
}