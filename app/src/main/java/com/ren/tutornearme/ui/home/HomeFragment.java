package com.ren.tutornearme.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ren.tutornearme.R;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.databinding.FragmentHomeBinding;
import com.ren.tutornearme.util.GPSHelper;
import com.ren.tutornearme.util.SnackBarHelper;

import static com.ren.tutornearme.util.Common.ZAM_LAT;
import static com.ren.tutornearme.util.Common.ZAM_LONG;
import static com.ren.tutornearme.util.SnackBarHelper.showSnackBar;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FragmentHomeBinding binding;
    private SupportMapFragment mapFragment;

    private static final int LOCATION_INTERVAL = 5000;
    private static final int LOCATION_MAX_WAIT_TIME = 10000;
    private static final int LOCATION_FASTEST_INTERVAL = 3000;
    private static final float LOCATION_MIN_DISTANCE = 10f;
    private static final float ZOOM_VAL = 18f;

    private HomeViewModel homeViewModel;
    private View mContainerView;
    private Context mContext;
    private FragmentActivity mActivity;
    private boolean isGpsEnabled = false;

    private final ValueEventListener onlineValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists())
                homeViewModel.getCurrentUserRef().onDisconnect().removeValue();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            SnackBarHelper.showSnackBar(mContainerView, "[ERROR]: " + error.getMessage());
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        if (context instanceof Activity){
            mActivity = (FragmentActivity) context;
        }
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mContext = null;
        super.onDetach();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        mContainerView = container;

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initLocationRequestBuilder();
        initMapBinding();

        return root;
    }

    @Override
    public void onResume() {
        homeViewModel.getOnlineRef().addValueEventListener(onlineValueEventListener);
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        homeViewModel.removeTutorLocation();
        homeViewModel.getOnlineRef().removeEventListener(onlineValueEventListener);
        super.onDestroy();
    }

    @SuppressLint("MissingPermission")
    private void initFusedLocationProvider() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, ZOOM_VAL));

                homeViewModel.isTutorLocationSet(locationResult).observe(mActivity,
                        new Observer<DataOrException<Boolean, Exception>>() {
                            @Override
                            public void onChanged(DataOrException<Boolean, Exception> dataOrException) {
                                if (dataOrException.exception != null)
                                    SnackBarHelper.showSnackBar(mContainerView,
                                            "[ERROR]: " + dataOrException.exception.getMessage());

                                if (dataOrException.data)
                                    Toast.makeText(mContext, "You're Online!", Toast.LENGTH_SHORT)
                                            .show();
                            }
                        });

                super.onLocationResult(locationResult);
            }
        };
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void initMapBinding() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
        else
            Snackbar.make(mContainerView, "[Error]: Can't bind map fragment",
                    Snackbar.LENGTH_SHORT).show();
    }

    public final ActivityResultLauncher<IntentSenderRequest> gpsResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        isGpsEnabled = true;
                        Toast.makeText(mContext,
                                "Location enabled!", Toast.LENGTH_LONG).show();
                        // All required changes were successfully made
                    } else {
                        SnackBarHelper.showSnackBar(mContainerView,
                                "Location access denied");
                        // The user was asked to change settings, but chose not to
                    }
                }
            });

    private void initLocationRequestBuilder() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMinUpdateDistanceMeters(LOCATION_MIN_DISTANCE)
                .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
                .setMaxUpdateDelayMillis(LOCATION_MAX_WAIT_TIME)
                .build();

        new GPSHelper().turnOnGPS(locationRequest, mActivity.getApplication(),
                mContainerView, this, new GPSHelper.OnGpsListener() {
                    @Override
                    public void gpsStatus(boolean isGPSEnabled) {
                        isGpsEnabled = isGPSEnabled;
                    }
                });
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        })
                        .create()
                        .show();
            } // No explanation needed, we can request the permission.
            else requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

            return false;
        }
        return true;
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @SuppressLint("MissingPermission")
                @Override
                public void onActivityResult(Boolean result) {
                    // if permission granted after prompt
                    if (result) {
                        showLocationWithButton();
                    } else {
                        Snackbar.make(mContainerView,
                                "Location permission was denied."
                                , Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        try {
            boolean isSuccessful = googleMap.setMapStyle(MapStyleOptions
                    .loadRawResourceStyle(mContext, R.raw.uber_maps_style));
        } catch (Resources.NotFoundException e) {
            Log.d("MAP_PARSE_ERROR", "onMapReady: " + e.getMessage());
        }
        // if permission already granted
        if (checkLocationPermission()) {
            showLocationWithButton();
        } else {
            LatLng zam = new LatLng(ZAM_LAT, ZAM_LONG);
            mMap.addMarker(new MarkerOptions().position(zam).title("Marker in Zamboanga"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(zam));
        }

        View locationButton = ((View) mapFragment.requireView().findViewById(Integer.parseInt("1")).getParent())
                .findViewById(Integer.parseInt("2"));

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();

        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.setMargins(0, 0, 0, 0);
    }

    @SuppressLint("MissingPermission")
    private void showLocationWithButton() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        initFusedLocationProvider();
        mMap.setOnMyLocationButtonClickListener(() -> {

            fusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            Location location = task.getResult();
                            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, ZOOM_VAL));
                        } else {
                            if (task.getException() != null)
                                showSnackBar(mContainerView, String.format("[ERROR]: %s",
                                        task.getException().getMessage()));
                        }
                    })
                    .addOnFailureListener(e -> showSnackBar(mContainerView, String.format("[ERROR]: %s",
                            e.getMessage())));

            return true;
        });
    }
}
