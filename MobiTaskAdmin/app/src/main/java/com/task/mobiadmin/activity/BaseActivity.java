package com.task.mobiadmin.activity;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.task.mobiadmin.R;
import com.task.mobiadmin.di.BaseApp;
import com.task.mobiadmin.retrofitManager.ApiClient;
import com.task.mobiadmin.retrofitManager.ApiInterface;
import com.task.mobiadmin.retrofitManager.ApiManager;
import com.task.mobiadmin.retrofitManager.ResponseListener;
import com.task.mobiadmin.utils.Const;
import com.task.mobiadmin.utils.ImageUtils;
import com.task.mobiadmin.utils.NetworkUtil;
import com.task.mobiadmin.utils.PermissionsManager;
import com.task.mobiadmin.utils.PrefStore;
import com.task.mobiadmin.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;

/**
 * Created by Neeraj Narwal on 2/7/15.
 */
public class BaseActivity extends AppCompatActivity implements View.OnClickListener, ResponseListener {
    public Picasso picasso;
    public ApiInterface apiInterface;
    @Inject
    public PrefStore store;
    @Inject
    public ApiClient apiClient;
    @Inject
    public ApiManager apiManager;
    private Toast toast;
    private NetworksBroadcast networksBroadcast;
    private AlertDialog networkAlertDialog;
    private AlertDialog.Builder networkDialog;
    private boolean showing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApp.Companion.create().getInjection().inject(this);
//        initializeNetworkBroadcast();
        createPicassoDownloader();
        strictModeThread();
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        apiManager.instantiateProgressDialog(this);
        networkDialog = new AlertDialog.Builder(this);
        apiInterface = apiClient.getClient().create(ApiInterface.class);
        Fabric.with(this, new Crashlytics());
//        checkDate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        store.setBoolean(Const.FOREGROUND, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        store.setBoolean(Const.FOREGROUND, false);
    }

    public boolean initFCM() {
        boolean isAvailable = checkPlayServices();
        if (isAvailable) {
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            try {
                if (refreshedToken != null)
                    refreshedToken = new JSONObject(refreshedToken).getString("token");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            store.saveString(Const.DEVICE_TOKEN, refreshedToken);
            log("Token >>>>>>>  " + refreshedToken);
//            if (refreshedToken != null) {
//                check(refreshedToken);
//            }
        }
        return isAvailable;
    }

    private void createPicassoDownloader() {
        OkHttpClient okHttpClient = new OkHttpClient();
        try {
            okHttpClient.setCache(new Cache(getCacheDir(), 8 * 1024))
                    .setReadTimeout(30, TimeUnit.SECONDS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        picasso = new Picasso.Builder(this)
                .downloader(new OkHttpDownloader(okHttpClient))

// In case you need to log picasso logs
//               .loggingEnabled(true)
                .build();
    }

    private void initializeNetworkBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        networksBroadcast = new NetworksBroadcast();
        registerReceiver(networksBroadcast, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (networksBroadcast != null) {
                unregisterReceiver(networksBroadcast);
            }
        } catch (IllegalArgumentException e) {
            networksBroadcast = null;
        }
    }

    private void showNoNetworkDialog(String status) {
        if (!isFinishing()) {
            networkDialog.setTitle(getString(R.string.netwrk_status));
            networkDialog.setMessage(status);
            networkDialog.setPositiveButton(getString(R.string.retry), null);
            networkDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    networkAlertDialog = null;
                    initializeNetworkBroadcast();
                }
            });
            networkDialog.setCancelable(false);
            networkAlertDialog = networkDialog.create();
            if (!networkAlertDialog.isShowing())
                networkAlertDialog.show();
        }
    }

    protected void checkDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2017, Calendar.MARCH, 31);
        Calendar currentcal = Calendar.getInstance();
        if (currentcal.after(cal)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please contact : Admin");
            builder.setTitle("Demo Expired");
            builder.setCancelable(false);
            builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    exitFromApp();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, Const.PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                showToast(getString(R.string.this_device_is_not_supported), true);
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsManager.onPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageUtils.activityResult(requestCode, resultCode, data);
    }

    public void exitFromApp() {
        finish();
        finishAffinity();
    }

    public void hideSoftKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) this
                    .getSystemService(BaseActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception ignored) {
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null
                && activeNetworkInfo.isConnectedOrConnecting();

    }

    public void log(String string) {
        Utils.debugLog("BaseActivity", string);
    }

    public void showExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.are_you_sure_want_to_exit))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int id) {
                        finishAffinity();
                    }
                }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void showToast(String msg, boolean isError) {
        View view = View.inflate(this, R.layout.layout_toast, null);
        TextView toastTV = (TextView) view.findViewById(R.id.toastTV);
        if (isError) {
            LinearLayout parentLL = (LinearLayout) view.findViewById(R.id.parentLL);
            parentLL.setBackgroundColor(ContextCompat.getColor(this, R.color.Red));
        }
        toastTV.setText(msg);
        toast.setView(view);
        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
        toast.show();
    }

    private void strictModeThread() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public void transitionSlideInHorizontal() {
        overridePendingTransition(R.anim.slide_from_top,
                R.anim.slide_out_top);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onSuccess(Call call, Object object) {

    }

    @Override
    public void onError(Call call, int statusCode, String errorMessage, ResponseListener responseListener) {
        log("Failed with error code : " + statusCode);
        if (statusCode == Const.SOCKET_TIMEOUT) {
            showRetry(call, responseListener);
        } else if (statusCode == Const.ErrorCodes.SESSION_EXPIRE) {
            showToast("Your session has been expired. Please login again", false);
            Utils.logoutUser(this, store);
        } else if (statusCode == Const.ErrorCodes.USER_LIMIT_EXCEEDED) {
            showToast("Your user limit has exceeded. Kindly upgrade your plan", false);
        } else if (statusCode == Const.ErrorCodes.EXPIRY_ARRIVAL) {
            if (!showing) {
                showExpiryDialog();
                showing = true;
            }
        } else if (statusCode == Const.ErrorCodes.ACCOUNT_DISABLE) {
            showToast("Your account is disabled", true);
            if (store.getString(Const.SESSION_ID) != null)
                Utils.logoutUser(this, store);
        } else {
            showToast("" + errorMessage, true);
            call.cancel();
        }
    }

    private void showExpiryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Account Expire");
        builder.setMessage("Your subscription has been expired. Contact us for further using MobiTask\n\nEmail us at: sales@pugmarks.com\n\nCall us at: 1800 3000 7458");
        builder.setPositiveButton("Email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "sales@pugmarks.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "MobiTask Account expansion");
                startActivity(Intent.createChooser(emailIntent, "Open Using"));
            }
        });
        builder.setNegativeButton("Phone", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:1800 3000 7458"));
                startActivity(Intent.createChooser(callIntent, "Call using"));
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                showing = false;
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    private void showRetry(final Call call, final ResponseListener responseListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert !");
        builder.setMessage("Request timeout");
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Call call1 = call.clone();
                apiManager.makeApiCall(call1, responseListener);
            }
        });
        builder.show();
    }

    public class NetworksBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = NetworkUtil.getConnectivityStatus(context);
            if (status == NetworkUtil.TYPE_NOT_CONNECTED) {
                if (networkAlertDialog == null)
                    showNoNetworkDialog("Not connected to internet");
            } else {
                if (networkAlertDialog != null && networkAlertDialog.isShowing())
                    networkAlertDialog.dismiss();
            }
        }
    }
}
