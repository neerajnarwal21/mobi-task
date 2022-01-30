package com.task.mobiadmin.retrofitManager;

import com.google.gson.JsonObject;

import javax.inject.Singleton;

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
@Singleton
public interface ApiInterface {

    @Multipart
    @POST("comp_login")
    Call<JsonObject> login(@Part("email") RequestBody email,
                           @Part("password") RequestBody password,
                           @Part("deviceid") RequestBody deviceId);

    @Multipart
    @POST("signup")
    Call<JsonObject> signup(@Part("compname") RequestBody name,
                            @Part("phoneno") RequestBody phone,
                            @Part("address") RequestBody address,
                            @Part("email") RequestBody email,
                            @Part("password") RequestBody password,
                            @Part("devicetoken") RequestBody deviceToken,
                            @Part("userlimit") RequestBody userLimit,
                            @Part("u_type") RequestBody userType,
                            @Part("dev_id") RequestBody deviceId,
                            @Part("submit") RequestBody submit);

    @Multipart
    @POST("logout")
    Call<JsonObject> logout(@Part("sess_id") RequestBody sessionId);

    //Since app is in one session per device now. so profile will be loaded either in login or in update

//    @Multipart
//    @POST("get_profile")
//    Call<JsonObject> getProfile(@Part("sess_id") RequestBody sessionId);

    @Multipart
    @POST("forget")
    Call<JsonObject> forgotPassword(@Part("email") RequestBody email,
                                    @Part("user_type") RequestBody userType);

    @Multipart
    @POST("listusers")
    Call<JsonObject> listUsers(@Part("sess_id") RequestBody sessionId);

    @Multipart
    @POST("adduser")
    Call<JsonObject> createUser(@Part("sess_id") RequestBody sessionId,
                                @Part("user_name") RequestBody name,
                                @Part("user_phone") RequestBody phone,
                                @Part("user_email") RequestBody email,
                                @Part("user_password") RequestBody password,
                                @Part MultipartBody.Part imagePart);

    @Multipart
    @POST("updateuserphoto")
    Call<JsonObject> updateUserPic(@Part("sess_id") RequestBody sessionId,
                                   @Part("user_id") RequestBody userId,
                                   @Part MultipartBody.Part imagePart);

    @Multipart
    @POST("userdelete")
    Call<JsonObject> deleteUser(@Part("sess_id") RequestBody sessionId,
                                @Part("user_id") RequestBody userId);

    @Multipart
    @POST("createtask")
    Call<JsonObject> createTask(@Part("sess_id") RequestBody sessionId,
                                @Part("t_u_id") RequestBody userId,
                                @Part("t_heading") RequestBody heading,
                                @Part("t_des") RequestBody desc,
                                @Part("t_in_time") RequestBody inTime,
                                @Part("t_out_time") RequestBody outTime,
                                @Part("t_lat") RequestBody lat,
                                @Part("t_long") RequestBody lng,
                                @Part("t_address") RequestBody address,
                                @Part("is_approvable") RequestBody isApprovable,
                                @Part MultipartBody.Part imagePart);

    @Multipart
    @POST("updatetask")
    Call<JsonObject> updateTask(@Part("sess_id") RequestBody sessionId,
                                @Part("t_id") RequestBody taskId,
                                @Part("t_u_id") RequestBody userId,
                                @Part("t_heading") RequestBody heading,
                                @Part("t_des") RequestBody desc,
                                @Part("t_in_time") RequestBody inTime,
                                @Part("t_out_time") RequestBody outTime,
                                @Part("t_lat") RequestBody lat,
                                @Part("t_long") RequestBody lng,
                                @Part("t_address") RequestBody address,
                                @Part("is_approvable") RequestBody isApprovable,
                                @Part("t_status") RequestBody taskStatus,
                                @Part MultipartBody.Part imagePart);

    @Multipart
    @POST("deletetask")
    Call<JsonObject> deleteTask(@Part("sess_id") RequestBody sessionId,
                                @Part("tid") RequestBody taskId,
                                @Part("uid") RequestBody userId);

    @Multipart
    @POST("tasklist")
    Call<JsonObject> taskList(@Part("sess_id") RequestBody sessionId,
                              @Part("page_num") RequestBody pageNo,
                              @Part("task_status") RequestBody taskState,
                              @Part("device_token") RequestBody token);

    @Multipart
    @POST("singletask")
    Call<JsonObject> singleTask(@Part("sess_id") RequestBody sessionId,
                                @Part("curr_user_type") RequestBody currentUserType,
                                @Part("task_id") RequestBody taskId);

    @Multipart
    @POST("approvetask")
    Call<JsonObject> approveTask(@Part("sess_id") RequestBody sessId,
                                 @Part("t_id") RequestBody taskId);

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
    @POST("updateuserdata")
    Call<JsonObject> updateUser(@Part("sess_id") RequestBody sessionId,
                                @Part("user_id") RequestBody userId,
                                @Part("user_name") RequestBody name,
                                @Part("user_phone") RequestBody phone);

    @Multipart
    @POST("comp-update-profile")
    Call<JsonObject> updateProfile(@Part("sess_id") RequestBody userId,
                                   @Part("comp_name") RequestBody name,
                                   @Part("comp_phone") RequestBody phone,
                                   @Part("lat") RequestBody lat,
                                   @Part("long") RequestBody lng,
                                   @Part("comp_address") RequestBody address,
                                   @Part MultipartBody.Part imagePart);

    @Multipart
    @POST("comp_change-password")
    Call<JsonObject> changePassword(@Part("sess_id") RequestBody sessId,
                                    @Part("oldpassword") RequestBody oldPass,
                                    @Part("newpassword") RequestBody newPass);

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
    @POST("getuserlocation")
    Call<JsonObject> sendTaskUserLocation(@Part("userid") RequestBody userId,
                                          @Part("taskid") RequestBody taskId,
                                          @Part("latitude") RequestBody latitude,
                                          @Part("longitude") RequestBody longitude,
                                          @Part("address") RequestBody address,
                                          @Part("date") RequestBody dateTime);

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
