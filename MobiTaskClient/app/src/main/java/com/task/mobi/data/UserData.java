package com.task.mobi.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserData implements Serializable, Parcelable {

    public final static Parcelable.Creator<UserData> CREATOR = new Creator<UserData>() {


        @SuppressWarnings({
                "unchecked"
        })
        public UserData createFromParcel(Parcel in) {
            UserData instance = new UserData();
            instance.id = ((String) in.readValue((String.class.getClassLoader())));
            instance.cId = ((String) in.readValue((String.class.getClassLoader())));
            instance.name = ((String) in.readValue((String.class.getClassLoader())));
            instance.photo = ((String) in.readValue((String.class.getClassLoader())));
            instance.phone = ((String) in.readValue((String.class.getClassLoader())));
            instance.email = ((String) in.readValue((String.class.getClassLoader())));
            instance.sessionId = ((String) in.readValue((String.class.getClassLoader())));
            return instance;
        }

        public UserData[] newArray(int size) {
            return (new UserData[size]);
        }

    };
    @SerializedName("Id")
    @Expose
    public String id;
    @SerializedName("cid")
    @Expose
    public String cId;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("photo")
    @Expose
    public String photo;
    @SerializedName("phone")
    @Expose
    public String phone;
    @SerializedName("email")
    @Expose
    public String email;
    @SerializedName("sess_token")
    @Expose
    public String sessionId;

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(cId);
        dest.writeValue(name);
        dest.writeValue(photo);
        dest.writeValue(phone);
        dest.writeValue(email);
        dest.writeValue(sessionId);
    }

    public int describeContents() {
        return 0;
    }

}