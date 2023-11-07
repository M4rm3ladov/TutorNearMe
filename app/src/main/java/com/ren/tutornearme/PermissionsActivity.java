package com.ren.tutornearme;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.ren.tutornearme.auth.MainActivity;
import com.ren.tutornearme.util.GPSHelper;
import com.ren.tutornearme.util.LocationHelper;
import static com.ren.tutornearme.util.PermissionsHelper.isGPSPermissionGranted;
import static com.ren.tutornearme.util.PermissionsHelper.isLocationPermissionGranted;

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

    public final ActivityResultLauncher<String> locationPermissionRequestLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                // if permission granted after prompt
                if (result) {
                    if (isGPSPermissionGranted(this))
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

        if (!isLocationPermissionGranted(this))
            initLocationHelper();
        else if (!isGPSPermissionGranted(this))
            initGPSHelper();
        else
            startActivity(new Intent(PermissionsActivity.this, MainActivity.class));
    }

    private void initGPSHelper() {
        GPSHelper.showGPSPermissionRationale(null, getApplication(),
                findViewById(android.R.id.content), this, null);
    }

    private void initLocationHelper() {
        LocationHelper.showLocationPermissionRationale(this, null);
    }


}