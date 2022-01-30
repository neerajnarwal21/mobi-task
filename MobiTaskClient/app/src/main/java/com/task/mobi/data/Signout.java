package com.task.mobi.data;

/**
 * Created by neeraj on 3/10/17.
 */

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Signout implements Serializable, Parcelable {

    public final static Parcelable.Creator<Signout> CREATOR = new Creator<Signout>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Signout createFromParcel(Parcel in) {
            return new Signout(in);
        }

        public Signout[] newArray(int size) {
            return (new Signout[size]);
        }

    };
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
    @SerializedName("workdone")
    @Expose
    public String workdone;

    protected Signout(Parcel in) {
        this.signoutpicture = ((String) in.readValue((String.class.getClassLoader())));
        this.signoutdate = ((String) in.readValue((String.class.getClassLoader())));
        this.signoutLat = ((String) in.readValue((String.class.getClassLoader())));
        this.signoutLong = ((String) in.readValue((String.class.getClassLoader())));
        this.workdone = ((String) in.readValue((String.class.getClassLoader())));
    }

    public Signout() {
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(signoutpicture);
        dest.writeValue(signoutdate);
        dest.writeValue(signoutLat);
        dest.writeValue(signoutLong);
        dest.writeValue(workdone);
    }

    public int describeContents() {
        return 0;
    }

}
