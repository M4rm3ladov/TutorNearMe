package com.ren.tutornearme.ui.subject.subject_file;

import static com.ren.tutornearme.util.Common.TUTOR_SUBJECT_REFERENCE;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ren.tutornearme.data.DataOrException;

import java.util.HashMap;
import java.util.Map;

public class SubjectFilesRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final StorageReference storageRef, subjectCredentialRef;
    private final DatabaseReference tutorSubjectRef = db.getReference(TUTOR_SUBJECT_REFERENCE);
    public SubjectFilesRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseStorage.getInstance().setMaxUploadRetryTimeMillis(20000);
        storageRef = FirebaseStorage.getInstance().getReference();
        subjectCredentialRef = storageRef.child("credentials/" + firebaseAuth.getUid());
    }

    private FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public MutableLiveData<DataOrException<Map<String,Object>, Exception>> uploadCredential(Uri pdfUri) {
        MutableLiveData<DataOrException<Map<String, Object>, Exception>> mutableLiveData = new MutableLiveData<>();

        if (getCurrentUser() != null) {
            DataOrException<Map<String, Object>, Exception> dataOrException = new DataOrException<>();
            dataOrException.data = new HashMap<>();

            String filePostKey = db.getReference().child("temp").push().getKey();
            subjectCredentialRef
                    .child( "/"+ filePostKey).putFile(pdfUri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            subjectCredentialRef
                                    .child("/"+ filePostKey)
                                            .getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        dataOrException.data.put("isComplete", uri.toString());
                                        mutableLiveData.postValue(dataOrException);
                                    }).addOnFailureListener(e -> {
                                        dataOrException.exception = e;
                                        mutableLiveData.postValue(dataOrException);
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        dataOrException.exception = e;
                        mutableLiveData.postValue(dataOrException);
                    }).addOnProgressListener(snapshot -> {
                        double progress =
                                (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            dataOrException.data.put("progress", progress);
                        mutableLiveData.postValue(dataOrException);
                    });
        }
        return mutableLiveData;
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> updateCredential(String key) {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();

        if (getCurrentUser() != null) {
            DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

            subjectCredentialRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {

                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put("credential", uri.toString());
                        // update the file
                        tutorSubjectRef.child(key)
                                .updateChildren(updateData)
                                .addOnFailureListener(e -> {

                                    dataOrException.exception = e;
                                    mutableLiveData.postValue(dataOrException);
                                }).addOnSuccessListener(unused -> {

                                    dataOrException.data = true;
                                    mutableLiveData.postValue(dataOrException);
                                });
                    });
        }
        return mutableLiveData;
    }
}
