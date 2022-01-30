package com.task.mobiadmin.data;

/**
 * Created by Neeraj Narwal on 26/6/17.
 */

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DemoSerializedData implements Serializable, Parcelable {

    public final static Parcelable.Creator<DemoSerializedData> CREATOR = new Creator<DemoSerializedData>() {


        @SuppressWarnings({
                "unchecked"
        })
        public DemoSerializedData createFromParcel(Parcel in) {
            DemoSerializedData instance = new DemoSerializedData();
            instance.companyid = ((String) in.readValue((String.class.getClassLoader())));
            instance.name = ((String) in.readValue((String.class.getClassLoader())));
            instance.photo = ((String) in.readValue((String.class.getClassLoader())));
            instance.email = ((String) in.readValue((String.class.getClassLoader())));
            instance.isfav = ((String) in.readValue((String.class.getClassLoader())));
            return instance;
        }

        public DemoSerializedData[] newArray(int size) {
            return (new DemoSerializedData[size]);
        }

    };
    @SerializedName("companyid")
    @Expose
    public String companyid;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("photo")
    @Expose
    public String photo;
    @SerializedName("email")
    @Expose
    public String email;
    @SerializedName("isfav")
    @Expose
    public String isfav;

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(companyid);
        dest.writeValue(name);
        dest.writeValue(photo);
        dest.writeValue(email);
        dest.writeValue(isfav);
    }

    public int describeContents() {
        return 0;
    }

}
