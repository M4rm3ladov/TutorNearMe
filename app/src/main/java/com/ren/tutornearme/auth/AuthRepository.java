package com.ren.tutornearme.auth;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ren.tutornearme.HomeActivity;
import com.ren.tutornearme.MainActivity;
import com.ren.tutornearme.RegisterActivity;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.TutorInfo;
import com.ren.tutornearme.util.Common;

public class AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth.AuthStateListener authStateListener;
    private final CollectionReference collectionReference = db.collection(Common.TUTOR_INFO_REFERENCE);

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public MutableLiveData<Boolean> checkIfSignedIn() {
        MutableLiveData<Boolean> mutableLiveData = new MutableLiveData<>();

        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            mutableLiveData.postValue(true);
        } else {
            mutableLiveData.postValue(false);
        }

        return mutableLiveData;
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> checkIfRegistered() {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            // check if current user has registered info before
            DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();
            collectionReference
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot: task.getResult()) {
                                if (documentSnapshot.exists()) {
                                    // Store data to static tutor obj
                                    if (currentUser.getUid().equals(documentSnapshot.getString("uid"))) {
                                        Common.currentTutor = documentSnapshot.toObject(TutorInfo.class);
                                        dataOrException.data = true;
                                    } else
                                        dataOrException.data = false;
                                } else
                                    dataOrException.data = false;
                            }
                        } else {
                            dataOrException.exception = task.getException();
                        }
                        mutableLiveData.postValue(dataOrException);
                    }
                });
        }
        return mutableLiveData;
    }

}
