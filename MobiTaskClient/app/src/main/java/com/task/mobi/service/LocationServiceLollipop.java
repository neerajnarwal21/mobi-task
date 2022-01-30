package com.task.mobi.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;

import com.task.mobi.utils.Const;
import com.task.mobi.utils.PrefStore;
import com.task.mobi.utils.Utils;

/**
 * Created by neeraj on 28/5/18.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LocationServiceLollipop extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        log("In job Service");
        //Reschedule job
        Utils.startService(this, new PrefStore(this).getString(Const.TASK_ID),false);
        //Launching common Intent service
        startService(new Intent(LocationServiceLollipop.this, LocationIntentService.class));
        return true;
    }

    private void log(String s) {
        Utils.debugLog("JobService", s);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
