package com.ren.tutornearme;


import static com.ren.tutornearme.util.Common.CURRENT_USER;
import static com.ren.tutornearme.util.Common.RESUBMIT;
import static com.ren.tutornearme.util.Common.SUBMITTED;
import static com.ren.tutornearme.util.Common.UNVERIFIED;
import static com.ren.tutornearme.util.Common.VERIFIED;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.ren.tutornearme.auth.MainActivity;
import com.ren.tutornearme.data.NavButtonAsyncResponse;
import com.ren.tutornearme.databinding.ActivityHomeBinding;
import com.ren.tutornearme.model.TutorInfo;
import com.ren.tutornearme.util.InternetHelper;
import com.ren.tutornearme.util.SnackBarHelper;

import org.parceler.Parcels;

public class HomeActivity extends AppCompatActivity implements NavButtonAsyncResponse {
    private CardView tutorVerifiedCardView;
    private TextView tutorIsVerified;
    private ShapeableImageView tutorProfileAvatar;

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Resources res;
    private TutorInfo tutorInfo;
    private SharedViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initSetTutorInfo();
        initNetworkAvailability();
        initLayoutBinding();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START))
                    drawer.closeDrawer(GravityCompat.START);
                else
                    moveTaskToBack(true);
            }
        });
    }

    @Override
    protected void onResume() {
        initNetworkAvailability();
        sharedViewModel.setTutorAccountStatusListener();
        super.onResume();
    }

    @Override
    protected void onPause() {
        sharedViewModel.removeTutorAccountStatusListener();
        super.onPause();
    }

    private void initSetTutorInfo() {
        tutorInfo = Parcels.unwrap(getIntent().getParcelableExtra(CURRENT_USER));

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        sharedViewModel.setTutorInfo(tutorInfo);
    }

    private void initNetworkAvailability() {
        if (!InternetHelper.isOnline(getApplication())) {
            Snackbar.make(findViewById(android.R.id.content),
                    "No internet connection. Please check your network",
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private void initLayoutBinding() {
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarHome.toolbar);
        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        tutorVerifiedCardView = navigationView.getHeaderView(0).findViewById(R.id.tutor_verified_CardView);
        tutorIsVerified = navigationView.getHeaderView(0).findViewById(R.id.tutor_verified_textview);
        tutorProfileAvatar = navigationView.getHeaderView(0).findViewById(R.id.tutor_profile_imageview);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_subject)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.getMenu().findItem(R.id.nav_subject).setEnabled(false);

        res = getResources();
        setSignOutBuilder();
        setNameAndPhoneNumber();
        setVerifiedListener();
    }

    private void setVerifiedListener() {
        sharedViewModel.getTutorAccountStatus().observe(this, dataOrException -> {
            if (dataOrException.exception != null)
                SnackBarHelper.showSnackBar(findViewById(android.R.id.content),
                        dataOrException.exception.getMessage());

            if (dataOrException.data != null) {
                String accountStatus = dataOrException.data;
                String accountStatusColor = "#009688";
                switch (accountStatus) {
                    case UNVERIFIED:
                        accountStatusColor = "#F44336";
                        break;
                    case VERIFIED:
                        accountStatusColor = "#009688";
                        break;
                    case RESUBMIT:
                    case SUBMITTED:
                        accountStatusColor = "#FB7D42";
                        break;
                }
                tutorIsVerified.setText(accountStatus);
                tutorVerifiedCardView.setCardBackgroundColor(Color.parseColor(accountStatusColor));

                if (accountStatus.equals(UNVERIFIED) || accountStatus.equals(RESUBMIT)) {
                    navigationView.getMenu().findItem(R.id.nav_profile).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_subject).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_home).setVisible(false);
                    return;
                }

                navigationView.getMenu().findItem(R.id.nav_profile).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_subject).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_home).setVisible(true);
                if (!tutorInfo.getAvatar().isEmpty()) {
                    Glide.with(HomeActivity.this)
                            .load(tutorInfo.getAvatar())
                            .placeholder(R.mipmap.ic_logo)
                            .apply(new RequestOptions().override(100, 100))
                            .into(tutorProfileAvatar);
                }
            }
        });
    }

    private void setSignOutBuilder() {
        navigationView.getMenu().findItem(R.id.nav_sign_out).setOnMenuItemClickListener(menuItem -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setTitle("Sign Out")
                    .setMessage("Do you really wish to sign out?")
                    .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                    .setPositiveButton("Sign Out", (dialogInterface, i) -> signOutUser())
                    .setCancelable(false);

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(res.getColor(R.color.red));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(res.getColor(R.color.green));
            });
            dialog.show();
            return false;
        });
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setNameAndPhoneNumber() {
        sharedViewModel.getTutorInfoLiveData().observe(this, tutorInfo -> {
            View headerView = navigationView.getHeaderView(0);
            TextView tutorName = headerView.findViewById(R.id.nav_name_textview);
            TextView tutorPhone = headerView.findViewById(R.id.nav_phone_textview);

            String name = String.format(res.getString(R.string.tutor_name),
                    tutorInfo.getFirstName(), tutorInfo.getLastName());
            tutorName.setText(name);
            tutorPhone.setText(tutorInfo.getPhoneNumber());
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void setProfileButtonEnabled(boolean enabled) {
        navigationView.getMenu().findItem(R.id.nav_subject).setEnabled(!enabled);
    }
}