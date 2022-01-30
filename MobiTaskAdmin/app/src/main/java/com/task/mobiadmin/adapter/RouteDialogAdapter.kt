package com.task.mobiadmin.adapter

import android.location.Location
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.task.mobiadmin.R
import com.task.mobiadmin.activity.BaseActivity
import com.task.mobiadmin.data.RouteData
import com.task.mobiadmin.fragment.RouteDetailFragment
import com.task.mobiadmin.utils.Utils
import java.text.DecimalFormat
import java.util.*


class RouteDialogAdapter(internal var baseActivity: BaseActivity, private val routeDatas: ArrayList<RouteData>, private val routeDetailFragment: RouteDetailFragment) : RecyclerView.Adapter<RouteDialogAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_dialog_route, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val routeData = routeDatas[position]
        updateUI(position, holder)

        holder.locationTV.text = routeData.address
        holder.dateTimeTV.text = Utils.changeDateFormat(Utils.getCurrentTimeZoneTime(routeData.date, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss", "dd MMM, hh:mm a ")
        if (position == 0) {
            holder.lengthTV.text = "Start point"
        } else if (position <= routeDatas.size - 1) {
            val floats = FloatArray(5)
            Location.distanceBetween(routeDatas[position - 1].lat, routeDatas[position - 1].lng, routeData.lat, routeData.lng, floats)
            if (floats[0].toDouble() != 0.0) {
                val df = DecimalFormat("###.#")
                df.format((floats[0] / 1000).toDouble())
                holder.lengthTV.text = df.format((floats[0] / 1000).toDouble()) + " Km"
            } else {
                holder.lengthTV.text = "0 Km"
            }
        }
        holder.parentLL.tag = position
        holder.parentLL.setOnClickListener { v ->
            val pos = v.tag as Int
            if (pos == 0) {
                routeDetailFragment.focusMarker(routeDatas[0])
            } else if (pos == routeDatas.size - 1) {
                routeDetailFragment.focusMarker(routeDatas[routeDatas.size - 1])
            } else {
                routeDetailFragment.onLocationClick(routeDatas[pos])
            }
        }
    }

    fun updateUI(position: Int, holder: MyViewHolder) {
        if (position == 0) {
            holder.topV.visibility = View.GONE
            holder.bottomV.visibility = View.VISIBLE
            holder.centerIV.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_round_green))
        } else if (position == routeDatas.size - 1) {
            holder.topV.visibility = View.VISIBLE
            holder.bottomV.visibility = View.GONE
            holder.centerIV.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_round_red_orange))
        } else {
            holder.topV.visibility = View.VISIBLE
            holder.bottomV.visibility = View.VISIBLE
            holder.centerIV.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_round_red))
        }
    }

    override fun getItemCount() = routeDatas.size

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var locationTV: TextView
        var lengthTV: TextView
        var dateTimeTV: TextView
        var centerIV: ImageView
        var bottomV: View
        var topV: View
        var parentLL: LinearLayout

        init {
            parentLL = view.findViewById<View>(R.id.parentLL) as LinearLayout
            centerIV = view.findViewById<View>(R.id.centerIV) as ImageView
            dateTimeTV = view.findViewById<View>(R.id.dateTimeTV) as TextView
            locationTV = view.findViewById<View>(R.id.locationTV) as TextView
            lengthTV = view.findViewById<View>(R.id.lengthTV) as TextView
            topV = view.findViewById(R.id.topV)
            bottomV = view.findViewById(R.id.bottomV)
        }
    }
}