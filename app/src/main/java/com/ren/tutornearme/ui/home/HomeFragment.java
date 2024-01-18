package com.ren.tutornearme.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ren.tutornearme.R;
import com.ren.tutornearme.auth.MainActivity;
import com.ren.tutornearme.data.NavButtonAsyncResponse;
import com.ren.tutornearme.databinding.FragmentHomeBinding;
import com.ren.tutornearme.model.StudentGeo;
import com.ren.tutornearme.model.StudentInfo;
import com.ren.tutornearme.model.TutorSubject;
import com.ren.tutornearme.ui.subject.tutor_subject.TutorSubjectViewModel;
import com.ren.tutornearme.util.GPSHelper;
import com.ren.tutornearme.util.LocationHelper;
import com.ren.tutornearme.util.SnackBarHelper;

import static com.ren.tutornearme.util.Common.AVERAGE_TUTOR_FEE;
import static com.ren.tutornearme.util.Common.FARE_PER_KM;
import static com.ren.tutornearme.util.Common.LOCATION_FASTEST_INTERVAL;
import static com.ren.tutornearme.util.Common.LOCATION_INTERVAL;
import static com.ren.tutornearme.util.Common.LOCATION_MAX_WAIT_TIME;
import static com.ren.tutornearme.util.Common.LOCATION_MIN_DISTANCE;
import static com.ren.tutornearme.util.Common.VERIFIED;
import static com.ren.tutornearme.util.Common.ZAM_LAT;
import static com.ren.tutornearme.util.Common.ZAM_LONG;
import static com.ren.tutornearme.util.PermissionsHelper.isGPSPermissionGranted;
import static com.ren.tutornearme.util.PermissionsHelper.isLocationPermissionGranted;
import static com.ren.tutornearme.util.SnackBarHelper.showSnackBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FragmentHomeBinding binding;
    private SupportMapFragment mapFragment;

    private static final float ZOOM_VAL = 18f;
    private boolean isTutorWorking = false;
    private ImageButton workingImageButton;

    private TutorSubjectViewModel subjectViewModel;
    private HomeViewModel homeViewModel;
    private View mContainerView;
    private Context mContext;
    private FragmentActivity mActivity;

    private ConstraintLayout bottomSheetLayout;
    private BottomSheetBehavior bottomSheetBehavior;
    private Button acceptStudentButton;
    private TextView studentDistanceTextView, studentSubjectTextView, studentNameTextView,
            studentSessionTextView, studentFeeTextView;

    private final ValueEventListener onlineValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists() && homeViewModel.getCurrentUserRef() != null) {
                homeViewModel.getCurrentUserRef().onDisconnect().removeValue();
                homeViewModel.getTutorRequestRef().onDisconnect().removeValue();
                homeViewModel.getTutorWorkingRef().onDisconnect().removeValue();
            }
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
        mActivity = null;
        super.onDetach();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        subjectViewModel =
                new ViewModelProvider(this).get(TutorSubjectViewModel.class);

        mContainerView = mActivity.findViewById(android.R.id.content);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        workingImageButton = binding.workingImageButton;

        View root = binding.getRoot();

        initBottomSheet();
        initBindBottomSheetViews();
        initLocationRequestBuilder();
        initMapBinding();
        initSwitchToggleListener();
        checkIfTutorHasVerifiedSubject();
        initFusedLocationProvider();

        return root;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        homeViewModel.getOnlineRef().addValueEventListener(onlineValueEventListener);
        if(isGPSAndLocationPermissionGranted())
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        super.onResume();
    }

    @Override
    public void onPause() {
        subjectViewModel.removeTutorSubjectListener();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        homeViewModel.removeTutorLocation();
        homeViewModel.getOnlineRef().removeEventListener(onlineValueEventListener);
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        homeViewModel.setLocationButtonImage(workingImageButton.getDrawable());
        super.onDestroyView();
    }

    private void initBottomSheet() {
        bottomSheetLayout = binding.studentInfoBottomSheet;
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private void initBindBottomSheetViews() {
        acceptStudentButton = binding.homeBookButton;
        studentDistanceTextView = binding.homeStudentDistanceTextview;
        studentSubjectTextView = binding.homeStudentSubject;
        studentNameTextView = binding.homeStudentName;
        studentSessionTextView = binding.homeStudentSession;
        studentFeeTextView = binding.homeStudentFee;
    }

    @SuppressLint("MissingPermission")
    private void showStudentBottomSheet(StudentGeo studentGeo, TutorSubject tutorSubject, StudentInfo studentInfo) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Location currentLocation = task.getResult();

                float[] result = new float[2];
                Location.distanceBetween(studentGeo.getL().get(0), studentGeo.getL().get(1),
                        currentLocation.getLatitude(), currentLocation.getLongitude(), result);

                double distanceInM = Math.round(result[0] * 10.0) / 10.0;
                double distanceInKm = Math.round((distanceInM / 1000) * 10.0) / 10.0;
                double feeOnKm = (Math.round(distanceInKm) * FARE_PER_KM) + AVERAGE_TUTOR_FEE * tutorSubject.getSessionHours();
                double feeOnM = AVERAGE_TUTOR_FEE * tutorSubject.getSessionHours();

                studentDistanceTextView.setText((distanceInM >= 1000) ? distanceInKm + " km" :
                        distanceInM + " m");
                studentFeeTextView.setText((distanceInM >= 1000) ? "Php " + feeOnKm : "Php " + feeOnM);
            } else
                showSnackBar(mContainerView, task.getException().getMessage());
        });

        studentSubjectTextView.setText((String.format("%s | %s", tutorSubject.getSubjectInfo().getName(),
                tutorSubject.getSubjectInfo().getDescription())));
        studentNameTextView.setText(String.format(getString(R.string.tutor_name),
                studentInfo.getFirstName(),
                studentInfo.getLastName()));
        studentSessionTextView.setText(tutorSubject.getSessionHours() > 1 ?
                tutorSubject.getSessionHours() + " hrs -" : tutorSubject.getSessionHours() + " hr -");

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(studentGeo.getL().get(0), studentGeo.getL().get(1)))
                .flat(true)
                .title(String.format(getString(R.string.tutor_name),
                        tutorSubject.getTutorInfo().getFirstName(),
                        tutorSubject.getTutorInfo().getLastName()))
                .snippet(tutorSubject.getTutorInfo().getPhoneNumber())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.book_icon)));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(studentGeo.getL().get(0), studentGeo.getL().get(1)), ZOOM_VAL));

    }

    @SuppressLint("MissingPermission")
    private void initFusedLocationProvider() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                Location tutorLocation = new Location("");
                tutorLocation.setLongitude(locationResult.getLastLocation().getLongitude());
                tutorLocation.setLatitude(locationResult.getLastLocation().getLatitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, ZOOM_VAL));

                homeViewModel.getIsTutorWorking().observe(getViewLifecycleOwner(), isTutorWorking -> {
                    if (isTutorWorking)
                        homeViewModel.updateTutorWorkingLocation(tutorLocation)
                                .observe(getViewLifecycleOwner(), dataOrException -> {
                                    if (dataOrException.exception != null) {
                                        showSnackBar(mContainerView,
                                                dataOrException.exception.getMessage());
                                    }
                                });
                    else
                        homeViewModel.isTutorLocationSet(locationResult).observe(mActivity,
                                dataOrException -> {
                                    if (dataOrException.exception != null) {
                                        SnackBarHelper.showSnackBar(mContainerView,
                                                "[ERROR]: " + dataOrException.exception.getMessage());
                                        return;
                                    }

                                    if (dataOrException.data)
                                        Toast.makeText(mContext, "You're Online!", Toast.LENGTH_SHORT)
                                                .show();
                                });
                });
                /*try {
                    GeoJsonLayer layer = new GeoJsonLayer(mMap, R.raw.zambo_mid_res, mContext);
                    //layer.addLayerToMap();

                    GeoJsonMultiPolygon geoJsonMultiPolygon;
                    GeoJsonPolygon geoJsonPolygon;
                    String currentTutorBarangay = "";

                    for (Feature feature : layer.getFeatures()) {
                        if (feature.getGeometry().getGeometryType().equals("MultiPolygon")) {
                            geoJsonMultiPolygon = (GeoJsonMultiPolygon) feature.getGeometry();
                            geoJsonPolygon = geoJsonMultiPolygon.getPolygons().get(0);
                            Log.d("multipoly", "onLocationResult: " + geoJsonMultiPolygon.getPolygons());
                            Log.d("poly", "onLocationResult: " + geoJsonPolygon);
                        } else {
                            geoJsonPolygon = (GeoJsonPolygon) feature.getGeometry();
                        }

                        if (PolyUtil.containsLocation(newPosition, geoJsonPolygon.getOuterBoundaryCoordinates(), true)) {
                            currentTutorBarangay = feature.getProperty("ADM4_EN");
                            break;
                        }
                    }

                    homeViewModel.setTutorLocationRef(currentTutorBarangay);
                    Log.d("BarangayCurrent", "onLocationResult: " + currentTutorBarangay);
                } catch (IOException | JSONException e) {
                    throw new RuntimeException(e);
                }*/
            }
        };
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
    }

    private void initSwitchToggleListener() {

        if (homeViewModel.getLocationButtonImage() != null)
            workingImageButton.setImageDrawable(homeViewModel.getLocationButtonImage());

        workingImageButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                if (!isTutorWorking) {
                    ((NavButtonAsyncResponse) mActivity).setProfileButtonEnabled(true);

                    isTutorWorking = true;
                    workingImageButton.setImageResource(R.drawable.ic_location_on);
                    Toast.makeText(mContext,
                            "Awaiting student booking", Toast.LENGTH_SHORT).show();

                    getLastLocation();
                } else {
                    ((NavButtonAsyncResponse) mActivity).setProfileButtonEnabled(false);

                    isTutorWorking = false;
                    workingImageButton.setImageResource(R.drawable.ic_location_off );

                    homeViewModel.removeCustomerRequestListener();
                    homeViewModel.removeTutorLocation();

                    Toast.makeText(mContext,
                            "Tutor on break", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (isGPSAndLocationPermissionGranted())
            fusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, ZOOM_VAL));

                            try {
                                Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
                                List<Address> addressList;

                                addressList = geocoder.getFromLocation(location.getLatitude(),
                                        location.getLongitude(), 1);
                                String cityName = addressList.get(0).getLocality();

                                homeViewModel.setTutorLocationRef(cityName);

                                homeViewModel.checkStudentRequest(location).observe(getViewLifecycleOwner(),
                                        dataOrException -> {
                                            if (dataOrException.exception != null) {
                                                showSnackBar(mContainerView, String.format("[ERROR]: %s",
                                                        dataOrException.exception.getMessage()));
                                                return;
                                            }

                                            if (dataOrException.data != null) {
                                                boolean isTutorWorking = dataOrException.data;
                                                homeViewModel.setIsTutorWorking(isTutorWorking);

                                                if (isTutorWorking) {
                                                    getStudentRequestDetails();
                                                    workingImageButton.setVisibility(View.GONE);
                                                } else {
                                                    workingImageButton.setVisibility(View.VISIBLE);
                                                    mMap.clear();
                                                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, ZOOM_VAL));
                                                }
                                            }
                                        });

                                homeViewModel.getOnlineRef().addValueEventListener(onlineValueEventListener);
                            } catch (IOException e) {
                                showSnackBar(mContainerView, "[ERROR]: " + e.getMessage());
                            }
                        } else {
                            showDefaultLocation();
                            if (task.getException() != null)
                                showSnackBar(mContainerView, String.format("[ERROR]: %s",
                                        task.getException().getMessage()));
                        }
                    })
                    .addOnFailureListener(e -> showSnackBar(mContainerView, String.format("[ERROR]: %s",
                            e.getMessage())));
    }

    private void getStudentRequestDetails() {
        // get student geo
        homeViewModel.getStudentGeo().observe(this, studentGeo -> {
            if (studentGeo.exception != null) {
                SnackBarHelper.showSnackBar(mContainerView,
                        "[ERROR]: " + studentGeo.exception.getMessage());
                return;
            }

            if (studentGeo.data != null) {
                // get student info
                StudentGeo customerGeo = studentGeo.data;
                homeViewModel.getStudentInfo().observe(this, studentInfo -> {
                    if (studentInfo.exception != null) {
                        SnackBarHelper.showSnackBar(mContainerView,
                                "[ERROR]: " + studentInfo.exception.getMessage());
                        return;
                    }

                    if (studentInfo.data != null) {
                        // get tutor subject
                        StudentInfo customerInfo = studentInfo.data;
                        homeViewModel.getStudentTutorSubject().observe(this, tutorSubject -> {
                            if (tutorSubject.exception != null) {
                                SnackBarHelper.showSnackBar(mContainerView,
                                        "[ERROR]: " + tutorSubject.exception.getMessage());
                                return;
                            }

                            if (tutorSubject.data != null) {
                                TutorSubject customerTutorSubject = tutorSubject.data;
                                showStudentBottomSheet(customerGeo, customerTutorSubject, customerInfo);
                            }
                        });
                    }
                });
            }
        });
    }

    private void checkIfTutorHasVerifiedSubject() {
        subjectViewModel.getTutorSubjectList()
                .observe(mActivity, dataOrException -> {
                    if (dataOrException.exception != null) {
                        SnackBarHelper.showSnackBar(mContainerView,
                                "[ERROR]: " + dataOrException.exception.getMessage());
                        return;
                    }

                    if (dataOrException.data != null) {
                        ArrayList<TutorSubject> tutorSubjectArrayList = dataOrException.data;

                        for (TutorSubject tutorSubject : tutorSubjectArrayList) {
                            if (tutorSubject.getStatus().equals(VERIFIED) && tutorSubject.isAvailable()) {
                                workingImageButton.setEnabled(true);
                                return;
                            }
                        }
                        Toast.makeText(mContext,
                                "No verified or available subject currently."
                                , Toast.LENGTH_SHORT).show();
                        workingImageButton.setEnabled(false);
                    }
                });
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

    public final ActivityResultLauncher<IntentSenderRequest> gpsPermissionRequestLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK)
                    // All required changes were successfully made
                    startActivity(new Intent(mActivity, MainActivity.class));
                else
                    mActivity.finishAffinity();
            });

    public final ActivityResultLauncher<String> locationPermissionRequestLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                // if permission granted after prompt
                if (result) {
                    if (isGPSPermissionGranted(mContext))
                        startActivity(new Intent(mContext, MainActivity.class));
                    else
                        initGPSHelper();
                }
                else
                    startActivity(new Intent(mContext, MainActivity.class));
            }
    );

    private boolean isGPSAndLocationPermissionGranted() {
        if (!isLocationPermissionGranted(mContext)) {
            showSnackBar(mContainerView, "[ERROR]: Location access is turned off. ");
            initLocationHelper();
            return false;
        } else if (!isGPSPermissionGranted(mContext)) {
            showSnackBar(mContainerView, "[ERROR]: Location services is turned off. ");
            initGPSHelper();
            return false;
        }
        return true;
    }

    private void initLocationRequestBuilder() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, LOCATION_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMinUpdateDistanceMeters(LOCATION_MIN_DISTANCE)
                .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
                .setMaxUpdateDelayMillis(LOCATION_MAX_WAIT_TIME)
                .build();
    }

    private void initGPSHelper() {
        GPSHelper.showGPSPermissionRationale(locationRequest, mActivity.getApplication(),
                mContainerView, (AppCompatActivity) mActivity, this);
    }

    private void initLocationHelper() {
        LocationHelper.showLocationPermissionRationale(mContext, this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        googleMap.setMapStyle(MapStyleOptions
                    .loadRawResourceStyle(mContext, R.raw.uber_maps_style));

        showDefaultLocation();
        // if permission already granted
        if(isGPSAndLocationPermissionGranted()) {
            showLocationWithButton();
        }

        View locationButton = ((View) mapFragment.requireView().findViewById(Integer.parseInt("1")).getParent())
                .findViewById(Integer.parseInt("2"));

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();

        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.setMargins(0, 0, 0, 250);
    }

    @SuppressLint("MissingPermission")
    private void showLocationWithButton() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.setOnMyLocationButtonClickListener(() -> {

            fusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, ZOOM_VAL));

                        } else {
                            showDefaultLocation();
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

    private void showDefaultLocation() {
        LatLng userLatLng = new LatLng(ZAM_LAT, ZAM_LONG);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, ZOOM_VAL));
    }
}
