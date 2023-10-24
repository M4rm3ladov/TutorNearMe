package com.ren.tutornearme.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ren.tutornearme.data.DataOrException;
import static com.ren.tutornearme.util.Common.TUTOR_INFO_REFERENCE;


public class AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference tutorInfoRef = db.getReference(TUTOR_INFO_REFERENCE);

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public MutableLiveData<Boolean> checkIfSignedIn() {
        MutableLiveData<Boolean> mutableLiveData = new MutableLiveData<>();

        currentUser = getCurrentUser();
        if (currentUser != null) {
            mutableLiveData.postValue(true);
        } else {
            mutableLiveData.postValue(false);
        }

        return mutableLiveData;
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> checkIfRegistered() {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        currentUser = getCurrentUser();

        if (currentUser != null) {
            // check if current user has registered info before
            DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();
            tutorInfoRef.child(currentUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            dataOrException.data = snapshot.exists();
                            mutableLiveData.postValue(dataOrException);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            dataOrException.exception = error.toException();
                            mutableLiveData.postValue(dataOrException);
                        }
                    });
        }
        return mutableLiveData;
    }

}
