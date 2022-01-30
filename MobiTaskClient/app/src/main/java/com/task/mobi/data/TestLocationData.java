package com.task.mobi.data;

/**
 * Created by neeraj on 3/10/17.
 */


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TestLocationData implements Serializable, Parcelable {

    public final static Creator<TestLocationData> CREATOR = new Creator<TestLocationData>() {


        @SuppressWarnings({
                "unchecked"
        })
        public TestLocationData createFromParcel(Parcel in) {
            return new TestLocationData(in);
        }

        public TestLocationData[] newArray(int size) {
            return (new TestLocationData[size]);
        }

    };
    @SerializedName("location")
    @Expose
    public String location;
    @SerializedName("date")
    @Expose
    public String date;
    @SerializedName("latitude")
    @Expose
    public Double lat;
    @SerializedName("longitude")
    @Expose
    public Double lng;

    public TestLocationData(String loc, String date, Double lat, Double lng) {
        this.location = loc;
        this.date = date;
        this.lat = lat;
        this.lng = lng;
    }

    protected TestLocationData(Parcel in) {
        this.location = ((String) in.readValue((String.class.getClassLoader())));
        this.date = ((String) in.readValue((String.class.getClassLoader())));
        this.lat = ((Double) in.readValue((Double.class.getClassLoader())));
        this.lng = ((Double) in.readValue((Double.class.getClassLoader())));
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(location);
        dest.writeValue(date);
        dest.writeValue(lat);
        dest.writeValue(lng);
    }

    public int describeContents() {
        return 0;
    }

}
