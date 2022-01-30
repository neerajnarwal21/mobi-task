package com.task.mobi.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.task.mobi.utils.Const;
import com.task.mobi.utils.PrefStore;
import com.task.mobi.utils.Utils;

/**
 * Created by neeraj on 13/10/17.
 */
public class LocationService extends Service {

    private Handler handler = new Handler();
    private PrefStore store;
    private Runnable r = new Runnable() {
        @Override
        public void run() {
            log("I am running");
            if (store.getString(Const.TASK_ID) != null) {
                log("Rescheduled Service");
                startService(new Intent(LocationService.this, LocationIntentService.class));
                startThread();
            } else {
                log("Killed Service");
                stopSelf();
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        store = new PrefStore(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(r);
        handler.post(r);
        log("Service onStartCommand");
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(r);
    }

    private void startThread() {
        handler.postDelayed(r, Const.LOCATION_UPDATE_INTERVAL);
    }

    private void log(String s) {
        Utils.debugLog("Location Service >>>>>>>>>> ", ">>>>>> " + s);
    }

}
