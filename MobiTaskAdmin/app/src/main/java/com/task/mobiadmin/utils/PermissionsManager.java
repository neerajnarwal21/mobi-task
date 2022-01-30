package com.task.mobiadmin.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.task.mobiadmin.R;
import com.task.mobiadmin.activity.BaseActivity;

import java.util.ArrayList;

/**
 * Created by neeraj on 29/9/17.
 */
public class PermissionsManager {

    private static PermissionCallback permissionCallback;
    private static int reqCode;
    private static Activity activity;

    public static boolean checkPermissions(Activity activity, String[] perms, int requestCode, PermissionCallback permissionCallback) {
        PermissionsManager.permissionCallback = permissionCallback;
        PermissionsManager.reqCode = requestCode;
        PermissionsManager.activity = activity;
        ArrayList<String> permsArray = new ArrayList<>();
        boolean hasPerms = true;
        for (String perm : perms) {
            if (ContextCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                permsArray.add(perm);
                hasPerms = false;
            }
        }
        if (!hasPerms) {
            String[] permsString = new String[permsArray.size()];
            for (int i = 0; i < permsArray.size(); i++) {
                permsString[i] = permsArray.get(i);
            }
            ActivityCompat.requestPermissions(activity, permsString, 99);
            return false;
        } else
            return true;
    }

    public static void onPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        boolean permGrantedBool = false;
        switch (requestCode) {
            case 99:
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        try {
                            ((BaseActivity) activity).showToast(activity.getString(R.string.not_sufficient_permissions, activity.getString(R.string.app_name)), true);
                        } catch (Exception e) {
                            Toast.makeText(activity, activity.getString(R.string.not_sufficient_permissions, activity.getString(R.string.app_name)), Toast.LENGTH_SHORT).show();
                        }
                        permGrantedBool = false;
                        break;
                    } else {
                        permGrantedBool = true;
                    }
                }
                if (permGrantedBool) {
                    permissionCallback.onPermissionsGranted(reqCode);
                } else {
                    permissionCallback.onPermissionsDenied(reqCode);
                }
                break;
        }
    }

    public interface PermissionCallback {
        void onPermissionsGranted(int resultCode);

        void onPermissionsDenied(int resultCode);
    }

}
