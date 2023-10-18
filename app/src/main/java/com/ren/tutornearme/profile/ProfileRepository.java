package com.ren.tutornearme.profile;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.TutorInfo;
import static com.ren.tutornearme.util.Common.currentTutor;

import static com.ren.tutornearme.util.Common.TUTOR_INFO_REFERENCE;

import androidx.lifecycle.MutableLiveData;

public class ProfileRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection(TUTOR_INFO_REFERENCE);

    public ProfileRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> registerTutor(TutorInfo tutorInfo) {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();
            collectionReference.add(tutorInfo)
                .addOnCompleteListener(documentReference -> {
                    if (documentReference.isSuccessful()) {

                        documentReference.getResult().get()
                            .addOnCompleteListener(documentSnapshot -> {

                                if (documentSnapshot.isSuccessful()) {
                                    DocumentSnapshot tutorData = documentSnapshot.getResult();
                                    currentTutor = tutorData.toObject(TutorInfo.class);
                                    dataOrException.data = true;
                                } else {
                                    dataOrException.exception = documentSnapshot.getException();
                                }
                                mutableLiveData.postValue(dataOrException);
                            });
                    } else {
                        dataOrException.exception = documentReference.getException();
                    }
                    mutableLiveData.postValue(dataOrException);
                });
        }
        return mutableLiveData;
    }
}
