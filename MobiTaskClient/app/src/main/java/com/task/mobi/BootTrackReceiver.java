package com.task.mobi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.task.mobi.utils.Const;
import com.task.mobi.utils.PrefStore;
import com.task.mobi.utils.Utils;


public class BootTrackReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) && new PrefStore(context).getString(Const.TASK_ID) != null) {
//            Intent pushIntent = new Intent(context, LocationIntentService.class);
            Utils.startService(context, new PrefStore(context).getString(Const.TASK_ID),true);
        }
    }
}
