package com.task.mobiadmin.data

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by neeraj on 25/5/18.
 */

data class TaskSignData(
        @SerializedName("id") @Expose var id: String = "",
        @SerializedName("userid") @Expose var userid: String = "",
        @SerializedName("taskid") @Expose var taskid: String = "",
        @SerializedName("signinpicture") @Expose var signinpicture: String = "",
        @SerializedName("signindate") @Expose var signindate: String = "",
        @SerializedName("signin_lat") @Expose var signinLat: String = "",
        @SerializedName("signin_long") @Expose var signinLong: String = "",
        @SerializedName("signin_address") @Expose var signinAddress: String = "",
        @SerializedName("signoutpicture") @Expose var signoutpicture: String = "",
        @SerializedName("signoutdate") @Expose var signoutdate: String = "",
        @SerializedName("signout_lat") @Expose var signoutLat: String = "",
        @SerializedName("signout_long") @Expose var signoutLong: String = "",
        @SerializedName("signout_address") @Expose var signoutAddress: String = "",
        @SerializedName("workdone") @Expose var workdone: String = "") : Parcelable {
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
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(userid)
        parcel.writeString(taskid)
        parcel.writeString(signinpicture)
        parcel.writeString(signindate)
        parcel.writeString(signinLat)
        parcel.writeString(signinLong)
        parcel.writeString(signinAddress)
        parcel.writeString(signoutpicture)
        parcel.writeString(signoutdate)
        parcel.writeString(signoutLat)
        parcel.writeString(signoutLong)
        parcel.writeString(signoutAddress)
        parcel.writeString(workdone)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<TaskSignData> {
        override fun createFromParcel(parcel: Parcel): TaskSignData {
            return TaskSignData(parcel)
        }

        override fun newArray(size: Int): Array<TaskSignData?> {
            return arrayOfNulls(size)
        }
    }
}
