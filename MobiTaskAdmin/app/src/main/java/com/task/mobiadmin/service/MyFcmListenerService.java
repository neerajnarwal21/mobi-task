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

package com.task.mobiadmin.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.task.mobiadmin.BuildConfig;
import com.task.mobiadmin.R;
import com.task.mobiadmin.activity.MainActivity;
import com.task.mobiadmin.activity.SplashActivity;
import com.task.mobiadmin.utils.Const;
import com.task.mobiadmin.utils.PrefStore;
import com.task.mobiadmin.utils.Utils;

import java.util.Map;


public class MyFcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "MyFcmListenerService";
    PrefStore store;

    @Override
    public void onCreate() {
        super.onCreate();
        store = new PrefStore(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        log("" + remoteMessage);
        Map data = remoteMessage.getData();
        if (!data.isEmpty() && data.containsKey(Const.APP_UPDATE)) {
            int versionCode = Integer.parseInt(data.get(Const.NEW_APP_VERSION_CODE).toString());
            if (BuildConfig.VERSION_CODE < versionCode) {
                String message =  data.containsKey(Const.OPTIONAL_UPDATE_MESSAGE) ? data.get(Const.OPTIONAL_UPDATE_MESSAGE).toString() : "A new version is available.";
                Notification.Builder builder;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    builder = new Notification.Builder(this, Const.NOTI_CHANNEL_ID);
                } else {
                    builder = new Notification.Builder(this);
                }
                builder.setContentTitle(getString(R.string.app_name));
                builder.setContentText(message);
                builder.setStyle(new Notification.BigTextStyle().bigText(message));
                builder.setSmallIcon(R.mipmap.ic_notification);

                Intent intent = new Intent(this, SplashActivity.class);
                intent.putExtra("isUpdate", true);
                intent.putExtra("updateMessage", message);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);
                NotificationManager mNotificationManager = (NotificationManager) this
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(5, builder.build());
            }
        } else if (store.getString(Const.SESSION_ID) != null) {
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

            Intent intent = new Intent(this, SplashActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);


            String action = data.get("action").toString();
            NotificationManager mNotificationManager = (NotificationManager) this
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            switch (action) {
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
//        } else {
//            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                log(">>>>>>>>>>>>>>>>>>>>>> Granted ");
//
//                mGoogleApiClient = new GoogleApiClient.Builder(this)
//                        .addApi(LocationServices.API)
//                        .addConnectionCallbacks(this)
//                        .build();
//                mGoogleApiClient.connect();
//                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//            } else {
//                builder.setContentText("Please enable location permission to work app properly");
//                builder.setStyle(new NotificationCompat.BigTextStyle().bigText("Please enable location permission to work app properly"));
//                mNotificationManager.notify(2, builder.build());
//            }
//        }
        }
    }

    private void log(String s) {
        Utils.debugLog(TAG, s);
    }

}