package com.task.mobiadmin.data

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UserData(
        @SerializedName("Id") @Expose var id: String,
        @SerializedName("companyname") @Expose var companyname: String,
        @SerializedName("email") @Expose var email: String,
        @SerializedName("phoneno") @Expose var phoneno: String,
        @SerializedName("userlimit") @Expose var userlimit: String,
        @SerializedName("level") @Expose var level: String,
        @SerializedName("user_type") @Expose var userType: String,
        @SerializedName("status") @Expose var status: String,
        @SerializedName("photo") @Expose var photo: String,
        @SerializedName("plan") @Expose var plan: String,
        @SerializedName("device_id") @Expose var deviceId: String,
        @SerializedName("address") @Expose var address: String,
        @SerializedName("lat") @Expose var lat: String,
        @SerializedName("long") @Expose var lng: String,
        @SerializedName("active_time") @Expose var expiryTime: String,
        @SerializedName("sess_token") @Expose var sessionId: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(companyname)
        parcel.writeString(email)
        parcel.writeString(phoneno)
        parcel.writeString(userlimit)
        parcel.writeString(level)
        parcel.writeString(userType)
        parcel.writeString(status)
        parcel.writeString(photo)
        parcel.writeString(plan)
        parcel.writeString(deviceId)
        parcel.writeString(address)
        parcel.writeString(lat)
        parcel.writeString(lng)
        parcel.writeString(expiryTime)
        parcel.writeString(sessionId)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<UserData> {
        override fun createFromParcel(parcel: Parcel): UserData {
            return UserData(parcel)
        }

        override fun newArray(size: Int): Array<UserData?> {
            return arrayOfNulls(size)
        }
    }
}