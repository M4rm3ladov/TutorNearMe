package com.ren.tutornearme.util;

import static com.ren.tutornearme.util.Common.LOCATION_FASTEST_INTERVAL;
import static com.ren.tutornearme.util.Common.LOCATION_INTERVAL;
import static com.ren.tutornearme.util.Common.LOCATION_MAX_WAIT_TIME;
import static com.ren.tutornearme.util.Common.LOCATION_MIN_DISTANCE;

import android.app.Application;
import android.view.View;

import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.ren.tutornearme.PermissionsActivity;
import com.ren.tutornearme.ui.home.HomeFragment;

public class GPSHelper {

    public static void showGPSPermissionRationale(LocationRequest locationRequest, Application application,
                                                  View rootView, AppCompatActivity activity, Fragment fragment) {
        if (locationRequest == null)
            locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMinUpdateDistanceMeters(LOCATION_MIN_DISTANCE)
                .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
                .setMaxUpdateDelayMillis(LOCATION_MAX_WAIT_TIME)
                .build();

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

                                if (activity != null) {
                                    if (activity.getClass().equals(PermissionsActivity.class))
                                        ((PermissionsActivity) activity).gpsPermissionRequestLauncher.launch(intentSenderRequest);
                                }

                                if (fragment != null) {
                                    ((HomeFragment) fragment).gpsPermissionRequestLauncher.launch(intentSenderRequest);
                                }
                                //if (fragmentActivity.getClass().equals(HomeFragment.class))


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
}

