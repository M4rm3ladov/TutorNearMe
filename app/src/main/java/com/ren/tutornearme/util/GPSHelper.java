package com.ren.tutornearme.util;

import android.app.Application;
import android.view.View;

import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.ren.tutornearme.ui.home.HomeFragment;

public class GPSHelper {

    public  void turnOnGPS(LocationRequest locationRequest, Application application,
                                 View rootView, Fragment activity, OnGpsListener onGpsListener) {
        LocationSettingsRequest.Builder settingsBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        settingsBuilder.setNeedBle(true);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(application).checkLocationSettings(settingsBuilder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    if (onGpsListener != null) {
                        onGpsListener.gpsStatus(true);
                    }
                    // All location settings are satisfied. The client can initialize location
                    // requests here.

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                IntentSenderRequest intentSenderRequest = new IntentSenderRequest
                                        .Builder(resolvable.getResolution()).build();
                                if (activity.getClass().equals(HomeFragment.class))
                                    ((HomeFragment)activity).launcher.launch(intentSenderRequest);

                            } catch (ClassCastException e) {
                                SnackBarHelper.showSnackBar(rootView,
                                        "[ERROR]: Location could not be resolved. Go to settings to enable");
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            SnackBarHelper.showSnackBar(rootView,
                                    "[ERROR]: Location could not be resolved. Go to settings to enable");
                            break;
                    }
                }
            }
        });
    }
    public interface OnGpsListener {
        void gpsStatus(boolean isGPSEnabled);
    }
}

