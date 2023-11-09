package com.ren.tutornearme;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.ren.tutornearme.auth.MainActivity;
import com.ren.tutornearme.util.GPSHelper;
import com.ren.tutornearme.util.LocationHelper;
import static com.ren.tutornearme.util.PermissionsHelper.isGPSPermissionGranted;
import static com.ren.tutornearme.util.PermissionsHelper.isLocationPermissionGranted;
import static com.ren.tutornearme.util.PermissionsHelper.isNotificationPermissionGranted;

public class PermissionsActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> settingsResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {});

    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (!isGranted) {
                new AlertDialog.Builder(PermissionsActivity.this)
                        .setTitle(R.string.title_notification_permission)
                        .setMessage(R.string.text_notification_service_permission)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setData(uri);
                            settingsResultLauncher.launch(intent);
                            finishAffinity();
                        })
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> finishAffinity())
                        .setCancelable(false)
                        .create()
                        .show();
        }
    });

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
                            .setMessage(R.string.text_location_permission)
                            .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setData(uri);
                                settingsResultLauncher.launch(intent);
                                finishAffinity();
                            })
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

        if (!isNotificationPermissionGranted(this)) {
            if (Build.VERSION.SDK_INT >= 33)
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        }

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