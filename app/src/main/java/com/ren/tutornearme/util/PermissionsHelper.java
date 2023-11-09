package com.ren.tutornearme.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

public class PermissionsHelper {

    public static Boolean isGPSPermissionGranted(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean isLocationPermissionGranted(Context context)
    {
        int result = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isNotificationPermissionGranted(Context context)
    {
        if (Build.VERSION.SDK_INT >= 33) {
            int result = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS);
            return result == PackageManager.PERMISSION_GRANTED;
        } else  {
            NotificationManagerCompat.from(context).areNotificationsEnabled();
            return false;
        }
    }
}
