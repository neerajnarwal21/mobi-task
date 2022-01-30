package com.task.mobiadmin.data

/**
 * Created by neeraj on 30/5/18.
 */

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CommentsData(
        @SerializedName("id") @Expose var id: String = "",
        @SerializedName("toName") @Expose var toName: String = "",
        @SerializedName("fromName") @Expose var fromName: String = "",
        @SerializedName("mTo") @Expose var mTo: String = "",
        @SerializedName("mFrom") @Expose var mFrom: String = "",
        @SerializedName("message") @Expose var message: String = "",
        @SerializedName("mTime") @Expose var mTime: String = "",
        var msgDirection: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    constructor(id: String) : this()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(toName)
        parcel.writeString(fromName)
        parcel.writeString(mTo)
        parcel.writeString(mFrom)
        parcel.writeString(message)
        parcel.writeString(mTime)
        parcel.writeString(msgDirection)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<CommentsData> {
        override fun createFromParcel(parcel: Parcel): CommentsData {
            return CommentsData(parcel)
        }

        override fun newArray(size: Int): Array<CommentsData?> {
            return arrayOfNulls(size)
        }
    }
}

