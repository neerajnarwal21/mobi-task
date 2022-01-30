package com.task.mobiadmin.data

/**
 * Created by neeraj on 3/10/17.
 */


import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RouteData(
        @SerializedName("latitude") @Expose var lat: Double = 0.0,
        @SerializedName("longitude") @Expose var lng: Double = 0.0,
        @SerializedName("address") @Expose var address: String,
        @SerializedName("date") @Expose var date: String,
        var distance: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(lat)
        parcel.writeDouble(lng)
        parcel.writeString(address)
        parcel.writeString(date)
        parcel.writeString(distance)
    }

    override fun describeContents()= 0

    companion object CREATOR : Parcelable.Creator<RouteData> {
        override fun createFromParcel(parcel: Parcel): RouteData {
            return RouteData(parcel)
        }

        override fun newArray(size: Int): Array<RouteData?> {
            return arrayOfNulls(size)
        }
    }
}