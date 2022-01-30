package com.task.mobiadmin.data

/**
 * Created by neeraj on 3/10/17.
 */


import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class EmpData(
        @SerializedName("Id") @Expose var id: Int? = null,
        @SerializedName("cid") @Expose var cid: Int? = null,
        @SerializedName("name") @Expose var name: String = "",
        @SerializedName("email") @Expose var email: String = "",
        @SerializedName("photo") @Expose var photo: String = "",
        @SerializedName("phone") @Expose var phone: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeValue(cid)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(photo)
        parcel.writeString(phone)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<EmpData> {
        override fun createFromParcel(parcel: Parcel): EmpData {
            return EmpData(parcel)
        }

        override fun newArray(size: Int): Array<EmpData?> {
            return arrayOfNulls(size)
        }
    }
}
