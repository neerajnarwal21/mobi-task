package com.task.mobi.utils.maps;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.task.mobi.utils.Const;
import com.task.mobi.utils.PermissionsManager;
import com.task.mobi.utils.Utils;

/**
 * Created by neeraj on 26/7/17.
 */
public class LocationManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, PermissionsManager.PermissionCallback {

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private PendingResult<LocationSettingsResult> result;
    private FusedLocationProviderClient mFusedLocationClient;
    private Activity activity;
    private LocationUpdates locationUpdates;

    private Accuracy accuracy;
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location location = locationResult.getLocations().get(0);
            log("Location : " + location.getLatitude() + " " + location.getLongitude() + " " + locationResult.getLocations().size());
            locationUpdates.onLocationFound(location);
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }

        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
            if (!locationAvailability.isLocationAvailable()) {
                log("Location not found");
                locationUpdates.onLocationNotFound();
                mFusedLocationClient.removeLocationUpdates(locationCallback);
            }
        }
    };

    public void startLocationManager(Activity activity, Accuracy accuracy, LocationUpdates locationUpdates) {
        this.locationUpdates = locationUpdates;
        this.activity = activity;
        this.accuracy = accuracy;
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (PermissionsManager.checkPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 122, this)) {
            mLocationRequest = LocationRequest.create();
            if (accuracy.equals(Accuracy.HIGH)) {
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            } else {
                mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
            }
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            builder.setAlwaysShow(true);

            result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult result) {
                    Status status = result.getStatus();
                    //final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            log("Success");
                            getLocation();
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            //...
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            log("Required");
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                if (accuracy.equals(Accuracy.HIGH))
                                    status.startResolutionForResult(
                                            activity,
                                            Const.REQUEST_LOCATION);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            log("Fail");
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            //...
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void log(String s) {
        Utils.debugLog("Location Manager: ", s);
    }

    public void onLocationEnable() {
        if (PermissionsManager.checkPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123, this)) {
            getLocation();
        }
    }

    public void getLocation() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, null);
        locationUpdates.onStartingGettingLocation();
//        mFusedLocationClient.getLastLocation()
//                .addOnSuccessListener(new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                         Got last known location. In some rare situations this can be null.
//                        log("Location >>>>>>>>. " + location);
//                        if (location != null) {
//                            locationUpdates.onLocationFound(location);
//                        } else {
//                            locationUpdates.onLocationNotFound();
//                        }
//                    }
//                });
    }

    @Override
    public void onPermissionsGranted(int resultCode) {
        if (resultCode == 123)
            onLocationEnable();
        else if (resultCode == 122) {
            onConnected(null);
        }
    }

    @Override
    public void onPermissionsDenied(int resultCode) {
        locationUpdates.onLocationPermissionDenied();
    }

    public enum Accuracy {
        HIGH,
        LOW
    }

    public interface LocationUpdates {

        void onStartingGettingLocation();

        void onLocationFound(Location location);

        void onLocationNotFound();

        void onLocationPermissionDenied();
    }
}
