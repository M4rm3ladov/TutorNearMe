package com.ren.tutornearme.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.ren.tutornearme.PermissionsActivity;
import com.ren.tutornearme.R;
import com.ren.tutornearme.ui.home.HomeFragment;

public class LocationHelper {
    public static void showLocationPermissionRationale(Context context, Fragment fragment) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale((AppCompatActivity) context, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            if (fragment == null) {
                                if (context.getClass().equals(PermissionsActivity.class))
                                    ((PermissionsActivity) (AppCompatActivity) context).locationPermissionRequestLauncher
                                            .launch(Manifest.permission.ACCESS_FINE_LOCATION);
                            } else {
                                ((HomeFragment) fragment).locationPermissionRequestLauncher
                                        .launch(Manifest.permission.ACCESS_FINE_LOCATION);
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } // No explanation needed, we can request the permission.
            else
                if (context.getClass().equals(PermissionsActivity.class))
                    ((PermissionsActivity) (AppCompatActivity)context).locationPermissionRequestLauncher
                        .launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }
}
