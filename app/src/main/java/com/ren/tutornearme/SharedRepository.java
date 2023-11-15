package com.ren.tutornearme;

import static com.ren.tutornearme.util.Common.TUTOR_INFO_REFERENCE;

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
import com.ren.tutornearme.model.TutorInfo;

public class SharedRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference tutorInfoRef = db.getReference(TUTOR_INFO_REFERENCE);

    public SharedRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public MutableLiveData<DataOrException<TutorInfo, Exception>> getTutorInfo() {
        MutableLiveData<DataOrException<TutorInfo, Exception>> mutableLiveData = new MutableLiveData<>();

        if (getCurrentUser() != null) {
            DataOrException<TutorInfo, Exception> dataOrException = new DataOrException<>();

            tutorInfoRef.child(getCurrentUser().getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists())
                                dataOrException.data = snapshot.getValue(TutorInfo.class);
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

    public MutableLiveData<DataOrException<String, Exception>> checkIfTutorVerified() {
        MutableLiveData<DataOrException<String, Exception>> mutableLiveData = new MutableLiveData<>();

        if (getCurrentUser() != null) {
            DataOrException<String, Exception> dataOrException = new DataOrException<>();

            tutorInfoRef.child(getCurrentUser().getUid()).child("accountStatus")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists())
                                dataOrException.data = snapshot.getValue(String.class);
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
