package com.task.mobi.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TaskData implements Serializable, Parcelable {

    public final static Parcelable.Creator<TaskData> CREATOR = new Creator<TaskData>() {


        @SuppressWarnings({
                "unchecked"
        })
        public TaskData createFromParcel(Parcel in) {
            TaskData instance = new TaskData();
            instance.id = ((String) in.readValue((String.class.getClassLoader())));
            instance.userid = ((String) in.readValue((String.class.getClassLoader())));
            instance.heading = ((String) in.readValue((String.class.getClassLoader())));
            instance.taskDescription = ((String) in.readValue((String.class.getClassLoader())));
            instance.inTime = ((String) in.readValue((String.class.getClassLoader())));
            instance.outTime = ((String) in.readValue((String.class.getClassLoader())));
            instance.address = ((String) in.readValue((String.class.getClassLoader())));
            instance.latitude = ((String) in.readValue((String.class.getClassLoader())));
            instance.longitude = ((String) in.readValue((String.class.getClassLoader())));
            instance.status = ((Integer) in.readValue((String.class.getClassLoader())));
            instance.audioFile = ((String) in.readValue((String.class.getClassLoader())));
            instance.downloadedFile = ((String) in.readValue((String.class.getClassLoader())));
            instance.taskNum = ((String) in.readValue((String.class.getClassLoader())));
            instance.taskSignData = ((TaskSignData) in.readValue((TaskSignData.class.getClassLoader())));
            return instance;
        }

        public TaskData[] newArray(int size) {
            return (new TaskData[size]);
        }

    };
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("userid")
    @Expose
    public String userid;
    @SerializedName("heading")
    @Expose
    public String heading;
    @SerializedName("task_description")
    @Expose
    public String taskDescription;
    @SerializedName("in_time")
    @Expose
    public String inTime;
    @SerializedName("out_time")
    @Expose
    public String outTime;
    @SerializedName("address")
    @Expose
    public String address;
    @SerializedName("latitude")
    @Expose
    public String latitude;
    @SerializedName("longitude")
    @Expose
    public String longitude;
    @SerializedName("status")
    @Expose
    public int status;
    @SerializedName("audio_file")
    @Expose
    public String audioFile;
    @SerializedName("unreadMsg")
    @Expose
    public int unreadMsg;
    @SerializedName("task_detail")
    @Expose
    public TaskSignData taskSignData;
    @Expose
    public String downloadedFile;
    public String taskNum;

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(userid);
        dest.writeValue(heading);
        dest.writeValue(taskDescription);
        dest.writeValue(inTime);
        dest.writeValue(outTime);
        dest.writeValue(address);
        dest.writeValue(latitude);
        dest.writeValue(longitude);
        dest.writeValue(status);
        dest.writeValue(audioFile);
        dest.writeValue(downloadedFile);
        dest.writeValue(taskNum);
        dest.writeValue(taskSignData);
    }

    public int describeContents() {
        return 0;
    }

}
