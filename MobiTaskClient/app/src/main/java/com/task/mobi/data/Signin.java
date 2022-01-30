package com.task.mobi.data;

/**
 * Created by neeraj on 3/10/17.
 */


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Signin implements Serializable, Parcelable {

    public final static Parcelable.Creator<Signin> CREATOR = new Creator<Signin>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Signin createFromParcel(Parcel in) {
            return new Signin(in);
        }

        public Signin[] newArray(int size) {
            return (new Signin[size]);
        }

    };
    @SerializedName("signpicture")
    @Expose
    public String signpicture;
    @SerializedName("signdate")
    @Expose
    public String signdate;
    @SerializedName("signin_lat")
    @Expose
    public String signinLat;
    @SerializedName("signin_long")
    @Expose
    public String signinLong;

    protected Signin(Parcel in) {
        this.signpicture = ((String) in.readValue((String.class.getClassLoader())));
        this.signdate = ((String) in.readValue((String.class.getClassLoader())));
        this.signinLat = ((String) in.readValue((String.class.getClassLoader())));
        this.signinLong = ((String) in.readValue((String.class.getClassLoader())));
    }

    public Signin() {
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(signpicture);
        dest.writeValue(signdate);
        dest.writeValue(signinLat);
        dest.writeValue(signinLong);
    }

    public int describeContents() {
        return 0;
    }

}
