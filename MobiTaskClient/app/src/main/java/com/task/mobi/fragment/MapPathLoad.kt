package com.task.mobi.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.task.mobi.R
import com.task.mobi.activity.BaseActivity
import com.task.mobi.data.RouteData
import com.task.mobi.data.TaskData
import com.task.mobi.retrofitManager.ApiClient
import com.task.mobi.retrofitManager.ApiInterface
import com.task.mobi.retrofitManager.ApiManager
import com.task.mobi.retrofitManager.ResponseListener
import com.task.mobi.utils.Const
import com.task.mobi.utils.PrefStore
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import java.util.*

/**
 * Created by neeraj on 28/5/18.
 */
class MapPathLoad(googleMap: GoogleMap, baseActivity: BaseActivity, clickV: View, emptyTV: TextView, taskData: TaskData) {
    var googleMap: GoogleMap
    var apiManager: ApiManager
    var apiInterface: ApiInterface
    var clickV: View
    var emptyTV: TextView
    var store: PrefStore
    var taskData: TaskData

    init {
        this.googleMap = googleMap

        apiManager = ApiManager.getInstance(baseActivity)
        apiInterface = ApiClient.getClient(baseActivity).create(ApiInterface::class.java)
        this.store = PrefStore(baseActivity)

        this.clickV = clickV
        this.emptyTV = emptyTV
        this.taskData = taskData
        loadPath()
        clickV.setOnClickListener({
            if (datas.size > 0) {
                val fragment = RouteDetailFragment()
                val bundle = Bundle()
                bundle.putParcelable("data", taskData)
                bundle.putParcelableArrayList("datas", datas)
                fragment.setArguments(bundle)
                baseActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_container, fragment)
                        .addToBackStack(null)
                        .commit()
            }
        })
    }

    private var pathCall: Call<JsonObject>? = null
    private var datas = ArrayList<RouteData>()

    private fun loadPath() {

        val sessId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val taskId = RequestBody.create(MediaType.parse("text/plain"), taskData.id)
        pathCall = apiInterface.getTaskRoute(sessId, taskId)
        apiManager.makeApiCall(pathCall, false, object : ResponseListener {
            override fun onSuccess(call: Call<*>?, `object`: Any?) {
                if (pathCall === call) {
                    val jsonObject = `object` as JsonObject
                    val data = jsonObject.getAsJsonArray("Data")

                    val objectType = object : TypeToken<ArrayList<RouteData>>() {

                    }.type
                    datas = Gson().fromJson<ArrayList<RouteData>>(data, objectType)
                    if (datas.size > 1) {
                        for (i in 0 until datas.size - 1) {
                            googleMap.addPolyline(PolylineOptions()
                                    .add(LatLng(datas.get(i).lat, datas.get(i).lng),
                                            LatLng(datas.get(i + 1).lat, datas.get(i + 1).lng))
                                    .color(Color.BLUE)
                                    .width(4f)
                                    .clickable(true))
                        }
                        googleMap.addMarker(MarkerOptions()
                                .position(LatLng(datas.get(0).lat, datas.get(0).lng))
                                .title("Start Position")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                        googleMap.addMarker(MarkerOptions()
                                .position(LatLng(datas.get(datas.size - 1).lat, datas.get(datas.size - 1).lng))
                                .title("End Position")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
                        val builder = LatLngBounds.Builder()
                        for (routeData in datas) {
                            builder.include(LatLng(routeData.lat, routeData.lng))
                        }
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 70))
                    }
                }
            }

            override fun onError(call: Call<*>?, statusCode: Int, errorMessage: String?, responseListener: ResponseListener?) {
                emptyTV.visibility = View.VISIBLE
            }
        })
    }

}