package com.ren.tutornearme;


import static com.ren.tutornearme.util.Common.CURRENT_USER;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.ren.tutornearme.databinding.ActivityHomeBinding;
import com.ren.tutornearme.model.TutorInfo;
import com.ren.tutornearme.util.InternetHelper;

import org.parceler.Parcels;

public class HomeActivity extends AppCompatActivity{

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Resources res;
    private TutorInfo tutorInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initSetTutorInfo();
        initNetworkAvailability();
        initLayoutBinding();
        initNavigationDrawer();
    }

    @Override
    protected void onResume() {
        initNetworkAvailability();
        super.onResume();
    }

    private void initSetTutorInfo() {
        tutorInfo = Parcels.unwrap(getIntent().getParcelableExtra(CURRENT_USER));

        SharedViewModel sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
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
    }

    private void initNavigationDrawer() {
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        res = getResources();
        setSignOutBuilder();
        setNameAndPhoneNumber();
    }

    private void setSignOutBuilder() {
        navigationView.getMenu().findItem(R.id.nav_sign_out).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("Sign Out")
                        .setMessage("Do you really wish to sign out?")
                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                        .setPositiveButton("Sign Out", (dialogInterface, i) -> {
                            signOutUser();
                        })
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
            }
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
        View headerView = navigationView.getHeaderView(0);
        TextView tutorName = headerView.findViewById(R.id.nav_name_textview);
        TextView tutorPhone = headerView.findViewById(R.id.nav_phone_textview);

        String name = String.format(res.getString(R.string.tutor_name),
                tutorInfo.getFirstName(), tutorInfo.getLastName());
        tutorName.setText(name);
        tutorPhone.setText(tutorInfo.getPhoneNumber());
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
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else if (navigationView.getMenu().findItem(R.id.nav_profile).isChecked())
            super.onBackPressed();
        else if(navigationView.getMenu().findItem(R.id.nav_home).isChecked())
            moveTaskToBack(true);
    }
}