package com.task.mobi.retrofitManager;

import com.google.gson.JsonObject;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
public interface ApiInterface {

    @Multipart
    @POST("login")
    Call<JsonObject> login(@Part("email") RequestBody email,
                           @Part("password") RequestBody password,
                           @Part("device_id") RequestBody deviceId);

    @Multipart
    @POST("logout")
    Call<JsonObject> logout(@Part("sess_id") RequestBody sessId);

    @Multipart
    @POST("forget")
    Call<JsonObject> forgotPassword(@Part("email") RequestBody email,
                                    @Part("user_type") RequestBody userType);

    @Multipart
    @POST("task")
    Call<JsonObject> getTasks(@Part("sess_id") RequestBody sessId,
                              @Part("page") RequestBody pageNo,
                              @Part("devicetoken") RequestBody deviceToken);
    @Multipart
    @POST("singletask")
    Call<JsonObject> singleTask(@Part("sess_id") RequestBody sessionId,
                                @Part("curr_user_type") RequestBody currentUserType,
                                @Part("task_id") RequestBody taskId);

    @Multipart
    @POST("update-profile")
    Call<JsonObject> updateProfile(@Part("sess_id") RequestBody sessId,
                                   @Part("name") RequestBody name,
                                   @Part("phone") RequestBody phone,
                                   @Part MultipartBody.Part imagePart);

    @Multipart
    @POST("change-password")
    Call<JsonObject> changePassword(@Part("sess_id") RequestBody sessId,
                                    @Part("oldpassword") RequestBody oldPass,
                                    @Part("newpassword") RequestBody newPass);


    @Multipart
    @POST("signin")
    Call<JsonObject> signInTask(@Part("sess_id") RequestBody sessId,
                                @Part("taskid") RequestBody taskId,
                                @Part("latitude") RequestBody latitude,
                                @Part("longitude") RequestBody longitude,
                                @Part("sign_address") RequestBody address,
                                @Part MultipartBody.Part imagePart);

    @Multipart
    @POST("signout")
    Call<JsonObject> signOutTask(@Part("sess_id") RequestBody sessId,
                                 @Part("taskid") RequestBody taskId,
                                 @Part("latitude") RequestBody latitude,
                                 @Part("longitude") RequestBody longitude,
                                 @Part("signout_address") RequestBody address,
                                 @Part("workdone") RequestBody workdone,
                                 @Part MultipartBody.Part imagePart);

    @Multipart
    @POST("recievemessages")
    Call<JsonObject> getMessages(@Part("sess_id") RequestBody sessId,
                                 @Part("t_id") RequestBody taskId,
                                 @Part("c_user_type") RequestBody currentUserType,
                                 @Part("page_num") RequestBody pageNo);

    @Multipart
    @POST("lastmsginsert")
    Call<JsonObject> updateLastMessage(@Part("sess_id") RequestBody sessId,
                                       @Part("t_id") RequestBody taskId,
                                       @Part("c_user_type") RequestBody currentUserType);

    @Multipart
    @POST("sendmessage")
    Call<JsonObject> sendMessage(@Part("sess_id") RequestBody sessId,
                                 @Part("m_to") RequestBody msgTo,
                                 @Part("m_to_type") RequestBody msgToType,
                                 @Part("msg") RequestBody msg,
                                 @Part("t_id") RequestBody taskId);
    @Multipart
    @POST("taskhistory")
    Call<JsonObject> oldTasks(@Part("sess_id") RequestBody sessId,
                              @Part("user_type") RequestBody userType,
                              @Part("page") RequestBody pageNo);

    @Multipart
    @POST("getlocation")
    Call<JsonObject> getLocation(@Part("userid") RequestBody userId,
                                 @Part("latitude") RequestBody latitude,
                                 @Part("longitude") RequestBody longitude);

    @Multipart
    @POST("updatetasklocation")
    Call<JsonObject> sendTaskUserLocation(@Part("sess_id") RequestBody sessId,
                                          @Part("taskid") RequestBody taskId,
                                          @Part("latitude") RequestBody latitude,
                                          @Part("longitude") RequestBody longitude,
                                          @Part("address") RequestBody address);

    @Multipart
    @POST("displayuseralllocation")
    Call<JsonObject> getTaskRoute(@Part("sess_id") RequestBody sessId,
                                  @Part("taskid") RequestBody taskId);

    @GET
    Observable<JsonObject> getDecodeAddress(@Url String url);

    // If API call is in type of Data object (RequestBody in general words) then send whole serialized model class in post

//    @POST("/api/fuel/RegisterUser")
//    Call<UserData> signup(@Body SignupData signupData);

}
