package com.task.mobi.data;

/**
 * Created by neeraj on 3/10/17.
 */


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RouteData implements Serializable, Parcelable {

    public final static Creator<RouteData> CREATOR = new Creator<RouteData>() {


        @SuppressWarnings({
                "unchecked"
        })
        public RouteData createFromParcel(Parcel in) {
            return new RouteData(in);
        }

        public RouteData[] newArray(int size) {
            return (new RouteData[size]);
        }

    };
    @SerializedName("latitude")
    @Expose
    public Double lat;
    @SerializedName("longitude")
    @Expose
    public Double lng;

    @SerializedName("address")
    @Expose
    public String address;

    @SerializedName("date")
    @Expose
    public String date;

    public String distance;

    protected RouteData(Parcel in) {
        this.lat = ((Double) in.readValue((Double.class.getClassLoader())));
        this.lng = ((Double) in.readValue((Double.class.getClassLoader())));
        this.distance = ((String) in.readValue((String.class.getClassLoader())));
        this.address = ((String) in.readValue((String.class.getClassLoader())));
        this.date = ((String) in.readValue((String.class.getClassLoader())));
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(lat);
        dest.writeValue(lng);
        dest.writeValue(distance);
        dest.writeValue(address);
        dest.writeValue(date);
    }

    public int describeContents() {
        return 0;
    }

}
