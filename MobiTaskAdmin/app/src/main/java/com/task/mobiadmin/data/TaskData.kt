package com.task.mobiadmin.data

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class TaskData(
        @SerializedName("id") @Expose var id: String = "",
        @SerializedName("userid") @Expose var userid: String = "",
        @SerializedName("heading") @Expose var heading: String = "",
        @SerializedName("task_description") @Expose var taskDescription: String = "",
        @SerializedName("in_time") @Expose var inTime: String = "",
        @SerializedName("out_time") @Expose var outTime: String = "",
        @SerializedName("address") @Expose var address: String = "",
        @SerializedName("latitude") @Expose var latitude: String = "",
        @SerializedName("longitude") @Expose var longitude: String = "",
        @SerializedName("status") @Expose var status: String = "",
        @SerializedName("is_approvable") @Expose var isApprovable: String = "",
        @SerializedName("audio_file") @Expose var audioFile: String = "",
        @SerializedName("unreadMsg") @Expose var unreadMsg: Int = 0,
        @SerializedName("user_data") @Expose var empData: EmpData = EmpData(),
        @SerializedName("task_detail") @Expose var taskSignData: TaskSignData? = null,
        @Expose var downloadedFile: String = "") : Parcelable {
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
            parcel.readInt(),
            parcel.readParcelable(EmpData::class.java.classLoader),
            parcel.readParcelable(TaskSignData::class.java.classLoader),
            parcel.readString())

    constructor(id: String) : this()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(userid)
        parcel.writeString(heading)
        parcel.writeString(taskDescription)
        parcel.writeString(inTime)
        parcel.writeString(outTime)
        parcel.writeString(address)
        parcel.writeString(latitude)
        parcel.writeString(longitude)
        parcel.writeString(status)
        parcel.writeString(isApprovable)
        parcel.writeString(audioFile)
        parcel.writeInt(unreadMsg)
        parcel.writeParcelable(empData, flags)
        parcel.writeParcelable(taskSignData, flags)
        parcel.writeString(downloadedFile)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<TaskData> {
        override fun createFromParcel(parcel: Parcel): TaskData {
            return TaskData(parcel)
        }

        override fun newArray(size: Int): Array<TaskData?> {
            return arrayOfNulls(size)
        }
    }
}
