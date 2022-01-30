package com.task.mobi.service;

import android.Manifest;
import android.app.IntentService;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.JsonObject;
import com.task.mobi.R;
import com.task.mobi.activity.SplashActivity;
import com.task.mobi.retrofitManager.ApiClient;
import com.task.mobi.retrofitManager.ApiInterface;
import com.task.mobi.retrofitManager.ApiManager;
import com.task.mobi.retrofitManager.ResponseListener;
import com.task.mobi.utils.Const;
import com.task.mobi.utils.PrefStore;
import com.task.mobi.utils.Utils;
import com.task.mobi.utils.maps.MapUtils;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * Created by neeraj on 13/10/17.
 */
public class LocationIntentService extends IntentService implements GoogleApiClient.ConnectionCallbacks, ResultCallback<LocationSettingsResult> {

    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private PrefStore store;
    private String taskid;
    private Location lastLocation;
    private float MIN_DISTANCE = 25.0f;
    private String addresss;
    LocationRequest mLocationRequest;

    public LocationIntentService() {
        super("Location");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        store = new PrefStore(this);
        taskid = store.getString(Const.TASK_ID);
        log("Service onHandleIntent");
        if (taskid != null) {

            getAndSendLocation();
        }
    }

    private void getAndSendLocation() {
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .build();
            mGoogleApiClient.connect();
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        } else {
            generateNoPermissionNotification();
        }
    }

    private void log(String s) {
        Utils.debugLog("Location Service", ">>>>>> " + s);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        if (Utils.isGPSon(this)) {
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(0.0001f);
            mLocationRequest.setInterval(1);
            mLocationRequest.setFastestInterval(10);
            mLocationRequest.setNumUpdates(2);
        } else {
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                log("Success");
                getLocation();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                log("Kyu aa gya ye error");
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mLocationRequest.getPriority() == LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(final Location location) {
                            log("Location >>>>>>>> " + location);
                            sendLocToServer(location);
                        }
                    });
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, null);
        }

    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location location = locationResult.getLocations().get(0);
            log("Location >>>>>>>> from GPS : " + location.getLatitude() + " " + location.getLongitude() + " " + locationResult.getLocations().size());
            sendLocToServer(location);
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    };

    private void sendLocToServer(Location location) {
        if (location != null) {
            if (store.containValue(Const.LAST_LOCATION))
                lastLocation = (Location) store.getUserData(Const.LAST_LOCATION, Location.class);
            if (lastLocation == null) {
                lastLocation = new Location(location);
                lastLocation.setLatitude(0.0);
                lastLocation.setLongitude(0.0);
                log("Initial location set");
            }
            log("Distance in locations is " + lastLocation.distanceTo(location));


//                            ArrayList<TestLocationData> locationDatas;
//                            PrefStore store = new PrefStore(LocationIntentService.this);
//                            if (store.getData("locDatass") == null) {
//                                locationDatas = new ArrayList<>();
//                            } else {
//                                locationDatas = store.getData("locDatass");
//                            }
//                            locationDatas.add(new TestLocationData(Utils.getAddress(location.getLatitude(), location.getLongitude(), getBaseContext())
//                                    , new SimpleDateFormat("HH:mm dd-MM-yy", Locale.ENGLISH).format(new Date())
//                                    , location.getLatitude()
//                                    , location.getLongitude()));
//                            store.setData("locDatass", locationDatas);
//
//                            log(">>>>>>>>>>>>>>. Location saved Size is : " + locationDatas.size());


            if (lastLocation.distanceTo(location) > MIN_DISTANCE) {
                log("Updating locations");
                lastLocation = location;
                store.saveUserData(Const.LAST_LOCATION, lastLocation);
                ApiManager apiManager = ApiManager.getInstance(LocationIntentService.this);
                ApiInterface apiInterface = ApiClient.getClient(LocationIntentService.this).create(ApiInterface.class);

                RequestBody sessId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID));
                RequestBody taskId = RequestBody.create(MediaType.parse("text/plain"), taskid);
                RequestBody lat = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(location.getLatitude()));
                RequestBody lng = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(location.getLongitude()));
                RequestBody address = RequestBody.create(MediaType.parse("text/plain"), getLocationAddress(location));

                Call<JsonObject> locationCall = apiInterface.sendTaskUserLocation(sessId, taskId, lat, lng, address);


                apiManager.makeApiCall(locationCall, false, new ResponseListener() {
                    @Override
                    public void onSuccess(Call call, Object object) {
                        log("Response >>>>>>>>>> " + object);

                    }

                    @Override
                    public void onError(Call call, int statusCode, String errorMessage, ResponseListener responseListener) {
                        if (statusCode == Const.ErrorCodes.SESSION_EXPIRE) {
                            Utils.logoutUserUsingService(LocationIntentService.this);
                        }
                    }
                });
            }
        }
        mGoogleApiClient.disconnect();
    }

    private String getLocationAddress(final Location location) {
        Observable.just(MapUtils.with(this).getAddressFromLatLng(location.getLatitude(), location.getLongitude()))
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String address) throws Exception {
                        log("After rx call single " + address);
                        if (!address.equals("Not Found")) {
                            addresss = address;
                        } else {
                            log("Starting rx call Address");
                            String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + location.getLatitude()
                                    + "," + location.getLongitude() + "&sensor=true";
                            ApiManager apiManager = ApiManager.getInstance(LocationIntentService.this);
                            ApiInterface apiInterface = ApiClient.getClient(LocationIntentService.this).create(ApiInterface.class);

                            Observable<JsonObject> call = apiInterface.getDecodeAddress(url);
                            call.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<JsonObject>() {
                                        @Override
                                        public void accept(JsonObject jsonObject) throws Exception {
                                            if (jsonObject.getAsJsonArray("results").size() > 0)
                                                addresss = jsonObject.getAsJsonArray("results").get(0).getAsJsonObject().get("formatted_address").getAsString();
                                        }
                                    });
                        }
                    }
                });
        return addresss;
    }

    private void generateNoPermissionNotification() {
        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, Const.NOTI_CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        builder.setContentTitle(getString(R.string.app_name));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentText("Please enable location permission to work app properly");
        builder.setStyle(new Notification.BigTextStyle().bigText("Please enable location permission to work app properly"));

        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null)
            mNotificationManager.notify(2, builder.build());
    }
}
