package com.ren.tutornearme;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;

import com.ren.tutornearme.auth.MainActivity;
import com.ren.tutornearme.util.GPSHelper;

public class PermissionsActivity extends AppCompatActivity {
    public final ActivityResultLauncher<IntentSenderRequest> gpsPermissionRequestLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK)
                    // All required changes were successfully made
                    startActivity(new Intent(PermissionsActivity.this, MainActivity.class));
                else
                    finishAffinity();
            });

    private final ActivityResultLauncher<String> locationPermissionRequestLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                // if permission granted after prompt
                if (result) {
                    if (isGPSEnabled(PermissionsActivity.this))
                        startActivity(new Intent(PermissionsActivity.this, MainActivity.class));
                    else
                        initGPSHelper();
                }
                else
                    new AlertDialog.Builder(PermissionsActivity.this)
                            .setTitle(R.string.title_location_permission)
                            .setMessage(R.string.text_location_permission_settings)
                            .setPositiveButton(R.string.ok, (dialogInterface, i) -> finishAffinity())
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> finishAffinity())
                            .setCancelable(false)
                            .create()
                            .show();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        if (!isLocationPermissionGranted())
            showLocationPermissionRationale();
        else if (!isGPSEnabled(PermissionsActivity.this))
            initGPSHelper();
        else startActivity(new Intent(PermissionsActivity.this, MainActivity.class));
    }

    private void initGPSHelper() {
        new GPSHelper().turnOnGPS(null, getApplication(),
                findViewById(android.R.id.content), this, new GPSHelper.OnGpsListener() {
                    @Override
                    public void gpsStatus(boolean isGPSEnabled) {

                    }
                });
    }

    private boolean isLocationPermissionGranted()
    {
        int result = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void showLocationPermissionRationale() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            locationPermissionRequestLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } // No explanation needed, we can request the permission.
            else
                locationPermissionRequestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    public static Boolean isGPSEnabled(Context context) {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is a new method provided in API 28
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        /*} else {
            // This was deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
        }*/
    }
}