package com.task.mobi.data;

/**
 * Created by neeraj on 30/5/18.
 */

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CommentsData implements Serializable, Parcelable {

    public final static Creator<CommentsData> CREATOR = new Creator<CommentsData>() {


        @SuppressWarnings({
                "unchecked"
        })
        public CommentsData createFromParcel(Parcel in) {
            return new CommentsData(in);
        }

        public CommentsData[] newArray(int size) {
            return (new CommentsData[size]);
        }

    };
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("toName")
    @Expose
    public String toName;
    @SerializedName("fromName")
    @Expose
    public String fromName;
    @SerializedName("mTo")
    @Expose
    public String mTo;
    @SerializedName("mFrom")
    @Expose
    public String mFrom;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("mTime")
    @Expose
    public String mTime;
    public String msgDirection;

    protected CommentsData(Parcel in) {
        this.id = ((String) in.readValue((String.class.getClassLoader())));
        this.toName = ((String) in.readValue((String.class.getClassLoader())));
        this.fromName = ((String) in.readValue((String.class.getClassLoader())));
        this.mTo = ((String) in.readValue((String.class.getClassLoader())));
        this.mFrom = ((String) in.readValue((String.class.getClassLoader())));
        this.message = ((String) in.readValue((String.class.getClassLoader())));
        this.mTime = ((String) in.readValue((String.class.getClassLoader())));
        this.msgDirection = ((String) in.readValue((String.class.getClassLoader())));
    }

    public CommentsData(String id) {
        this.id = id;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(toName);
        dest.writeValue(fromName);
        dest.writeValue(mTo);
        dest.writeValue(mFrom);
        dest.writeValue(message);
        dest.writeValue(mTime);
        dest.writeValue(msgDirection);
    }

    public int describeContents() {
        return 0;
    }

}
