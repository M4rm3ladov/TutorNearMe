package com.ren.tutornearme.basic_info;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.TutorInfo;

import static com.ren.tutornearme.util.Common.TUTOR_INFO_REFERENCE;

import androidx.lifecycle.MutableLiveData;

public class BasicInfoRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference tutorInfoRef = db.getReference(TUTOR_INFO_REFERENCE);

    public BasicInfoRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    public MutableLiveData<DataOrException<TutorInfo, Exception>> saveTutorInfo(TutorInfo tutorInfo) {
        MutableLiveData<DataOrException<TutorInfo, Exception>> mutableLiveData = new MutableLiveData<>();

        if (getCurrentUser() != null) {
            DataOrException<TutorInfo, Exception> dataOrException = new DataOrException<>();

            tutorInfoRef.child(getCurrentUser().getUid()).setValue(tutorInfo)
                .addOnCompleteListener(documentReference -> {
                    if (documentReference.isSuccessful()) {
                        dataOrException.data = tutorInfo;
                    } else {
                        dataOrException.exception = documentReference.getException();
                    }
                    mutableLiveData.postValue(dataOrException);
                });
        }
        return mutableLiveData;
    }
}
