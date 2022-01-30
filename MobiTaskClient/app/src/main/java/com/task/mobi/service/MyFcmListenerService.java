/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.task.mobi.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.JsonObject;
import com.task.mobi.R;
import com.task.mobi.activity.MainActivity;
import com.task.mobi.activity.SplashActivity;
import com.task.mobi.retrofitManager.ApiClient;
import com.task.mobi.retrofitManager.ApiInterface;
import com.task.mobi.retrofitManager.ApiManager;
import com.task.mobi.retrofitManager.ResponseListener;
import com.task.mobi.utils.Const;
import com.task.mobi.utils.PrefStore;
import com.task.mobi.utils.Utils;

import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;


public class MyFcmListenerService extends FirebaseMessagingService implements GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "MyFcmListenerService";
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private PrefStore store;

    @Override
    public void onCreate() {
        super.onCreate();
        store = new PrefStore(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map data = remoteMessage.getData();
        log("" + data);
        if (store.getString(Const.SESSION_ID) != null) {
            Notification.Builder builder;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                builder = new Notification.Builder(this, Const.NOTI_CHANNEL_ID);
            } else {
                builder = new Notification.Builder(this);
            }
            builder.setContentTitle(getString(R.string.app_name));
            builder.setContentText(data.get("message").toString());
            builder.setStyle(new Notification.BigTextStyle().bigText(data.get("message").toString()));
            builder.setSmallIcon(R.mipmap.ic_notification);

            String action = data.get("action").toString();
            NotificationManager mNotificationManager = (NotificationManager) this
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            switch (action) {
                case "get-location":
                    if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        log(">>>>>>>>>>>>>>>>>>>>>> Granted ");

                        mGoogleApiClient = new GoogleApiClient.Builder(this)
                                .addApi(LocationServices.API)
                                .addConnectionCallbacks(this)
                                .build();
                        mGoogleApiClient.connect();
                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                    } else {
                        builder.setContentText("Please enable location permission to work app properly");
                        builder.setStyle(new Notification.BigTextStyle().bigText("Please enable location permission to work app properly"));
                        mNotificationManager.notify(2, builder.build());
                    }
                    break;
                case "task-created":
                    Intent intent = new Intent(this, SplashActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(pendingIntent);

                    mNotificationManager.notify(0, builder.build());
                    break;
                case "new-chat":
                    if (!store.getBoolean(Const.CHAT_FOREGROUND, false)) {
                        String toId = data.get("fromId").toString();
                        String taskId = data.get("taskId").toString();

                        Intent intentMain = new Intent(this, MainActivity.class);
                        intentMain.putExtra("taskId", taskId);
                        intentMain.putExtra("toId", toId);
                        intentMain.putExtra("fromPush", true);
                        intentMain.putExtra("action", "new-chat");
                        PendingIntent pendingIntentMain = PendingIntent.getActivity(this, 0, intentMain, PendingIntent.FLAG_UPDATE_CURRENT);
                        builder.setContentIntent(pendingIntentMain);

                        mNotificationManager.notify(1, builder.build());
                    } else {
                        Intent intent1 = new Intent(Const.NEW_MESSAGE_BROADCAST);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);
                    }
                    break;
            }
        }
    }

    private void log(String s) {
        Utils.debugLog(TAG, s);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        log("Success");
                        getLocation();
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    public void getLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        log("Location >>>>>>>>. " + location);
                        if (location != null) {
                            ApiManager apiManager = ApiManager.getInstance(MyFcmListenerService.this);
                            ApiInterface apiInterface = ApiClient.getClient(MyFcmListenerService.this).create(ApiInterface.class);

                            RequestBody userId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.USER_ID));
                            RequestBody lat = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(location.getLatitude()));
                            RequestBody lng = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(location.getLongitude()));

                            Call<JsonObject> locationCall = apiInterface.getLocation(userId, lat, lng);
                            apiManager.makeApiCall(locationCall, false, new ResponseListener() {
                                @Override
                                public void onSuccess(Call call, Object object) {
                                    log("Response >>>>>>>>>> " + object);
                                    mGoogleApiClient.disconnect();
                                }

                                @Override
                                public void onError(Call call, int statusCode, String errorMessage, ResponseListener responseListener) {
                                    mGoogleApiClient.disconnect();
                                }
                            });
                        }
                    }
                });
    }
}