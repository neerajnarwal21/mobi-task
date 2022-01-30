package com.task.mobiadmin.fragment

import com.google.android.gms.maps.model.LatLng

/**
 * Created by neeraj.narwal on 9/8/16.
 */
class PinAdd {

    private var listener: AddPinListener? = null

    fun setListener(listener: AddPinListener) {
        this.listener = listener
    }

    fun sendBackLocation(address: String, latLng: LatLng) {
        if (listener != null)
            listener!!.onLocationPinAdded(address, latLng)
    }

    interface AddPinListener {
        fun onLocationPinAdded(address: String, latLng: LatLng)
    }

    companion object {
        val instance = PinAdd()
    }
}
