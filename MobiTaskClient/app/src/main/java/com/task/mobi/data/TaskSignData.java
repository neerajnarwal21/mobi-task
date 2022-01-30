package com.task.mobi.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by neeraj on 25/5/18.
 */

public class TaskSignData implements Serializable, Parcelable {

    public final static Creator<TaskSignData> CREATOR = new Creator<TaskSignData>() {


        @SuppressWarnings({
                "unchecked"
        })
        public TaskSignData createFromParcel(Parcel in) {
            return new TaskSignData(in);
        }

        public TaskSignData[] newArray(int size) {
            return (new TaskSignData[size]);
        }

    };
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("userid")
    @Expose
    public String userid;
    @SerializedName("taskid")
    @Expose
    public String taskid;
    @SerializedName("signinpicture")
    @Expose
    public String signinpicture;
    @SerializedName("signindate")
    @Expose
    public String signindate;
    @SerializedName("signin_lat")
    @Expose
    public String signinLat;
    @SerializedName("signin_long")
    @Expose
    public String signinLong;
    @SerializedName("signin_address")
    @Expose
    public String signinAddress;
    @SerializedName("signoutpicture")
    @Expose
    public String signoutpicture;
    @SerializedName("signoutdate")
    @Expose
    public String signoutdate;
    @SerializedName("signout_lat")
    @Expose
    public String signoutLat;
    @SerializedName("signout_long")
    @Expose
    public String signoutLong;
    @SerializedName("signout_address")
    @Expose
    public String signoutAddress;
    @SerializedName("workdone")
    @Expose
    public String workdone;

    protected TaskSignData(Parcel in) {
        this.id = ((String) in.readValue((String.class.getClassLoader())));
        this.userid = ((String) in.readValue((String.class.getClassLoader())));
        this.taskid = ((String) in.readValue((String.class.getClassLoader())));
        this.signinpicture = ((String) in.readValue((String.class.getClassLoader())));
        this.signindate = ((String) in.readValue((String.class.getClassLoader())));
        this.signinLat = ((String) in.readValue((String.class.getClassLoader())));
        this.signinLong = ((String) in.readValue((String.class.getClassLoader())));
        this.signinAddress = ((String) in.readValue((String.class.getClassLoader())));
        this.signoutpicture = ((String) in.readValue((String.class.getClassLoader())));
        this.signoutdate = ((String) in.readValue((String.class.getClassLoader())));
        this.signoutLat = ((String) in.readValue((String.class.getClassLoader())));
        this.signoutLong = ((String) in.readValue((String.class.getClassLoader())));
        this.signoutAddress = ((String) in.readValue((String.class.getClassLoader())));
        this.workdone = ((String) in.readValue((String.class.getClassLoader())));
    }

    public TaskSignData() {
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(userid);
        dest.writeValue(taskid);
        dest.writeValue(signinpicture);
        dest.writeValue(signindate);
        dest.writeValue(signinLat);
        dest.writeValue(signinLong);
        dest.writeValue(signinAddress);
        dest.writeValue(signoutpicture);
        dest.writeValue(signoutdate);
        dest.writeValue(signoutLat);
        dest.writeValue(signoutLong);
        dest.writeValue(signoutAddress);
        dest.writeValue(workdone);
    }

    public int describeContents() {
        return 0;
    }

}
