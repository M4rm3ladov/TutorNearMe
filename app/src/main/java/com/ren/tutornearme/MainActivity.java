package com.ren.tutornearme;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.ren.tutornearme.auth.AuthViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private List<AuthUI.IdpConfig> providers = new ArrayList<>();
    private ActivityResultLauncher<Intent> signInLauncher;
    private AuthViewModel viewModel;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    /*private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(Common.TUTOR_INFO_REFERENCE);
    private boolean hasRegisteredData = false;*/

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        if (firebaseAuth != null && authStateListener != null)
            firebaseAuth.removeAuthStateListener(authStateListener);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RelativeLayout mainRelativeLayout = findViewById(R.id.main_relative_layout);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication()))
                .get(AuthViewModel.class);

        signInLauncher = registerForActivityResult(
                new FirebaseAuthUIActivityResultContract(),
                new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                    @Override
                    public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                        onSignInResult(result);
                    }
                }
        );

        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().setAllowedCountries(Collections.singletonList("ph")).build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                viewModel.checkIfSignedIn().observe(MainActivity.this, isSignedIn -> {
                    if (!isSignedIn)
                        showLoginLayout();
                    else
                        viewModel.checkIfRegistered().observe(MainActivity.this, dataOrException -> {
                            if (dataOrException.data != null) {
                                navigateTowards(dataOrException.data);
                            }

                            if (dataOrException.exception != null) {
                                Snackbar.make(mainRelativeLayout, "[ERROR]: " + dataOrException.exception.getMessage(),
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        });
                });
            }
        };


        /*authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {
                    // check if current user has registered info before
                    collectionReference.whereEqualTo("uid", currentUser.getUid())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                                    if (documentSnapshot.exists()) {
                                        //if (currentUser.getUid().equals(document.getString("uid"))) {
                                            // Store data to static tutor obj
                                        Common.currentTutor = documentSnapshot.toObject(TutorInfo.class);
                                        hasRegisteredData = true;
                                        break;
                                        //}
                                    }
                                }
                                Intent intent;
                                if (!hasRegisteredData) {
                                    // New user
                                    intent = new Intent(MainActivity.this, RegisterActivity.class);
                                } else {
                                    // Has registered data
                                    intent = new Intent(MainActivity.this, HomeActivity.class);
                                }
                                startActivity(intent);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(findViewById(android.R.id.content), "[ERROR]: "
                                        + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                hasRegisteredData = false;
                            }
                        });

                } else {
                    showLoginLayout();
                }
            }
        };*/
    }

    private void navigateTowards(Boolean data) {
        Intent intent;
        if (!data) {
            // New user
            intent = new Intent(MainActivity.this, RegisterActivity.class);
        } else {
            // Has registered data
            intent = new Intent(MainActivity.this, HomeActivity.class);
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
       /* if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            //currentUser = firebaseAuth.getCurrentUser();
        }*/
        if (result.getResultCode() != RESULT_OK) {

            // Sign in failed
            if (response == null) {
                // User pressed back button
                Snackbar.make(findViewById(android.R.id.content), "[ERROR]: "
                        + "Sign in cancelled", Snackbar.LENGTH_LONG).show();
                return;
            } else if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                Snackbar.make(findViewById(android.R.id.content), "[ERROR]: "
                        + "No internet connection", Snackbar.LENGTH_LONG).show();
                return;
            }

            Snackbar.make(findViewById(android.R.id.content), "[ERROR]: "
                    + "Unknown Error", Snackbar.LENGTH_LONG).show();
            Log.e("SIGN_IN_ERROR", "Sign-in error: ", response.getError());
        }
    }

}