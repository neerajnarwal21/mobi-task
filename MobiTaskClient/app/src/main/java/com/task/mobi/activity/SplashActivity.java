package com.task.mobi.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;

import com.task.mobi.R;
import com.task.mobi.utils.Const;
import com.task.mobi.utils.TokenRefresh;

/**
 * Created by Neeraj Narwal on 5/5/17.
 */

public class SplashActivity extends BaseActivity implements TokenRefresh.TokenListener {

    private Handler handler = new Handler();
    private Runnable waitingRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isFinishing() && store.getString(Const.DEVICE_TOKEN) == null) {
                showDialogNoServices();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        createNotificationChannel();
        log("Token >>>> Start app");
        if (initFCM())
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    log("Token >>>> After 2 sec checking token");
                    if (!isFinishing() && store.getString(Const.DEVICE_TOKEN) != null)
                        gotoMainActivity();
                    else {
                        registerForTokenCallback();
                        waitFor5SecMore();
                    }
                }
            }, 2000);

        NotificationManager mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            String channelId = Const.NOTI_CHANNEL_ID;
            String channelName = "Some Channel";
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            if (notificationManager != null)
                notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void showDialogNoServices() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert !");
        builder.setMessage("Unable to initialize Google Services." +
                "\nMake sure your Internet connection speed is good." +
                "\nYou can try restart your phone, check if google apps like maps, gmail working or not." +
                "\nIf still problem persists then contact app admin");
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                exitFromApp();
            }
        });
        builder.show();
    }

    private void waitFor5SecMore() {
        log("Token >>>> Token isn't here, Waiting for token for 10 sec");
        handler.postDelayed(waitingRunnable, 10000);
    }

    private void registerForTokenCallback() {
        TokenRefresh tokenRefresh = TokenRefresh.getInstance();
        tokenRefresh.setTokenListener(this);
    }

    @Override
    public void onTokenRefresh() {
        log("Token >>>> Token is here");
        handler.removeCallbacks(waitingRunnable);
        gotoMainActivity();
    }

    private void gotoMainActivity() {
        if (store.getString(Const.SESSION_ID) == null) {
            startActivity(new Intent(this, LoginForgotActivity.class));
        } else
            startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
