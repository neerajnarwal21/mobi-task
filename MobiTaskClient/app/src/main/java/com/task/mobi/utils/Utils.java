package com.task.mobi.utils;

import android.app.Activity;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.task.mobi.BuildConfig;
import com.task.mobi.activity.LoginForgotActivity;
import com.task.mobi.service.LocationService;
import com.task.mobi.service.LocationServiceLollipop;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by neeraj on 11/9/17.
 */
public class Utils {

    public static String changeDateFormat(String dateString, String sourceDateFormat, String targetDateFormat) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }
        SimpleDateFormat inputDateFromat = new SimpleDateFormat(sourceDateFormat, Locale.getDefault());
        Date date = new Date();
        try {
            date = inputDateFromat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat outputDateFormat = new SimpleDateFormat(targetDateFormat, Locale.getDefault());
        return outputDateFormat.format(date);
    }

    public static String changeDateFormatFromDate(Date sourceDate, String targetDateFormat) {
        if (sourceDate == null || targetDateFormat == null || targetDateFormat.isEmpty()) {
            return "";
        }
        SimpleDateFormat outputDateFormat = new SimpleDateFormat(targetDateFormat, Locale.getDefault());
        return outputDateFormat.format(sourceDate);
    }

    public static Date getDateFromStringDate(String dateString, String sourceDateFormat) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        SimpleDateFormat inputDateFromat = new SimpleDateFormat(sourceDateFormat, Locale.ENGLISH);
        Date date = new Date();
        try {
            date = inputDateFromat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getCurrentTimeZoneTime(String inputDate, String dateFormat) {
        DateFormat df = new SimpleDateFormat("z", Locale.getDefault());
        Date date = getDateFromStringDate(inputDate + " +0000", dateFormat + " Z");
        String localTime = df.format(date);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        TimeZone tz = TimeZone.getTimeZone(localTime);
        calendar.setTimeZone(tz);
        DateFormat format = new SimpleDateFormat(dateFormat, Locale.getDefault());

        return format.format(calendar.getTime());
    }

    public static String getUniqueDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static boolean isValidMail(String email) {
        return email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");
    }

    public static boolean isValidPassword(String password) {
        return password.matches("^(?=\\S+$).{8,}$");
    }

    public static void changeStatusBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(activity, color));
        }
    }

    public static void setStatusBarTranslucent(Activity activity, boolean makeTranslucent) {
        if (makeTranslucent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }
    }

    public static String getAddress(Location location, Context context) {
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context);
            if (location.getLatitude() != 0 || location.getLongitude() != 0) {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                String address = addresses.get(0).getAddressLine(0);
                String address1 = addresses.get(0).getAddressLine(1);
                String country = addresses.get(0).getCountryName();
                return address + ", " + address1 + ", " + (country != null ? country : "");
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getAddress(Double lat, Double lng, Context context) {
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context);
            if (lat != 0 || lng != 0) {
                addresses = geocoder.getFromLocation(lat, lng, 1);
                String address = addresses.get(0).getAddressLine(0);
                String address1 = addresses.get(0).getAddressLine(1);
                String country = addresses.get(0).getCountryName();
                return address + ", " + address1 + ", " + (country != null ? country : "");
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void debugLog(String tag, String s) {
        if (BuildConfig.DEBUG)
            Log.e(tag, s);
    }

    public static void keyHash(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash:>>>>>>>>>>>>>>>", "" + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void logoutUser(AppCompatActivity activity, PrefStore store) {

//Logout User
        store.saveString(Const.SESSION_ID, null);
        store.saveString(Const.USER_ID, null);
        store.saveUserData(Const.USER_DATA, null);
        activity.startActivity(new Intent(activity, LoginForgotActivity.class));
        stopService(activity);
        activity.finish();
    }

    public static void logoutUserUsingService(Service service) {

//Logout User using Service
        PrefStore store = new PrefStore(service);
        store.saveString(Const.SESSION_ID, null);
        store.saveString(Const.USER_ID, null);
        store.saveUserData(Const.USER_DATA, null);
        stopService(service);
        service.stopSelf();
    }

    public static void startService(Context context, String taskId, boolean isFirstRun) {
        PrefStore store = new PrefStore(context);
        store.saveString(Const.TASK_ID, taskId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) {
                jobScheduler.cancel(Const.LOCATION_TASK_JOB);
                jobScheduler.schedule(new JobInfo.Builder(Const.LOCATION_TASK_JOB,
                        new ComponentName(context, LocationServiceLollipop.class))
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setMinimumLatency(isFirstRun ? 1000 : Const.LOCATION_UPDATE_INTERVAL)
                        .setRequiresCharging(false)
                        .setRequiresDeviceIdle(false)
                        .build());
            }
        } else {
            Intent intent = new Intent(context, LocationService.class);
            context.startService(intent);
        }
    }

    public static void stopService(Context context) {
        PrefStore store = new PrefStore(context);
        store.saveString(Const.TASK_ID, null);
        store.saveUserData(Const.LAST_LOCATION, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) {
                jobScheduler.cancel(Const.LOCATION_TASK_JOB);
            }
        } else {
            context.stopService(new Intent(context, LocationService.class));
        }
    }

    public static boolean isGPSon(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
