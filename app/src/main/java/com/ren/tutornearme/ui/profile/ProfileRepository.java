package com.ren.tutornearme.ui.profile;

import static com.ren.tutornearme.util.Common.TUTOR_INFO_REFERENCE;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ren.tutornearme.data.DataOrException;

import java.util.HashMap;
import java.util.Map;


public class ProfileRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final StorageReference storageRef;
    private final StorageReference avatarRef;
    private final DatabaseReference tutorInfoRef = db.getReference(TUTOR_INFO_REFERENCE);

    public ProfileRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        avatarRef = storageRef.child("avatars/" + firebaseAuth.getUid());
    }

    private FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public MutableLiveData<DataOrException<Map<String,Object>, Exception>> uploadAvatar(Uri imageUri) {
        MutableLiveData<DataOrException<Map<String, Object>, Exception>> mutableLiveData = new MutableLiveData<>();

        if (getCurrentUser() != null) {
            DataOrException<Map<String, Object>, Exception> dataOrException = new DataOrException<>();
            dataOrException.data = new HashMap<>();
            avatarRef.putFile(imageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                dataOrException.data.put("isComplete", true);
                                mutableLiveData.postValue(dataOrException);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dataOrException.exception = e;
                            mutableLiveData.postValue(dataOrException);
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress =
                                    (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            dataOrException.data.put("progress", progress);
                            mutableLiveData.postValue(dataOrException);
                        }
                    });

        }
        return mutableLiveData;
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> updateAvatar() {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();

        if (getCurrentUser() != null) {
            DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

            avatarRef.getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("avatar", uri.toString());

                            tutorInfoRef.child(getCurrentUser().getUid())
                                    .updateChildren(updateData)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dataOrException.exception = e;
                                            mutableLiveData.postValue(dataOrException);
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            dataOrException.data = true;
                                            mutableLiveData.postValue(dataOrException);
                                        }
                                    });
                        }
                    });
        }
        return mutableLiveData;
    }

}
