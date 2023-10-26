package com.ren.tutornearme.auth;

import static com.ren.tutornearme.util.Common.CURRENT_USER;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.snackbar.Snackbar;
import com.ren.tutornearme.HomeActivity;
import com.ren.tutornearme.R;
import com.ren.tutornearme.model.TutorInfo;
import com.ren.tutornearme.register.RegisterActivity;
import com.ren.tutornearme.util.InternetHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<AuthUI.IdpConfig> providers = new ArrayList<>();
    private ActivityResultLauncher<Intent> signInLauncher;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initAuthViewModel();
        initFirebaseAuthUI();
        initNetworkAvailability();
        initAuthListener();
    }

    private void initNetworkAvailability() {
        if (!InternetHelper.isOnline(getApplication())) {
            Snackbar.make(findViewById(android.R.id.content),
                    "[ERROR]: No internet connection. Please check your network",
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private void initAuthListener() {

        authViewModel.checkIfSignedIn().observe(MainActivity.this, isSignedIn -> {
            if (!isSignedIn)
                showLoginLayout();
            else
                authViewModel.checkIfRegistered().observe(MainActivity.this, dataOrException -> {
                    if (dataOrException.exception != null) {
                        showLoginLayout();
                        Snackbar.make(findViewById(android.R.id.content),
                                "[ERROR]: " + dataOrException.exception.getMessage(),
                                Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    navigateTowards(dataOrException.data);
                });
        });
    }

    private void initFirebaseAuthUI() {
        signInLauncher = registerForActivityResult(
                new FirebaseAuthUIActivityResultContract(),
                result -> onSignInResult(result)
        );

        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().setAllowedCountries(Collections.singletonList("ph")).build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
    }

    private void initAuthViewModel() {
        authViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication()))
                .get(AuthViewModel.class);
    }

    private void navigateTowards(TutorInfo tutorInfo) {
        Intent intent;
        if (tutorInfo == null)
            // New user
            intent = new Intent(MainActivity.this, RegisterActivity.class);
        else {
            // Has registered data
            intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.putExtra(CURRENT_USER, tutorInfo);
        }
        startActivity(intent);
        finish();
    }

    private void showLoginLayout() {
        AuthMethodPickerLayout authMethodPickerLayout = new AuthMethodPickerLayout
                .Builder(R.layout.activity_main)
                .setPhoneButtonId(R.id.get_started_button)
                .setGoogleButtonId(R.id.google_sign_in_button)
                .build();

        signInLauncher.launch(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setIsSmartLockEnabled(false)
                .setLockOrientation(true)
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers)
                .build());
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();

        if (result.getResultCode() == RESULT_OK) {
            initAuthListener();
        } else {
            if (response == null) {
                // User pressed back button
                finishAffinity();
                return;
            }

            if (response.getError() != null) {
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK)
                    Snackbar.make(findViewById(android.R.id.content), "[ERROR]: "
                        + "[ERROR]: No internet connection", Snackbar.LENGTH_LONG).show();
                else
                    Snackbar.make(findViewById(android.R.id.content), "[ERROR]: "
                            + "[ERROR]: Unknown Error", Snackbar.LENGTH_LONG).show();
            }

            showLoginLayout();
        }
    }

}