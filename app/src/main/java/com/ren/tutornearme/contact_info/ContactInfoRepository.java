package com.ren.tutornearme.contact_info;

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

import java.util.HashMap;
import java.util.Map;

public class ContactInfoRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference tutorInfoRef = db.getReference(TUTOR_INFO_REFERENCE);

    public ContactInfoRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> checkIfEmailExists(String email) {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

        tutorInfoRef.orderByChild("email").equalTo(email)
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
        return mutableLiveData;
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> updateEmail(String email) {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("email", email);

        tutorInfoRef.child(getCurrentUser().getUid())
                .updateChildren(updateData)
                .addOnCompleteListener(task -> {
                    dataOrException.data = task.isSuccessful();
                    mutableLiveData.postValue(dataOrException);
                })
                .addOnFailureListener(e -> {
                    dataOrException.exception = e;
                    mutableLiveData.postValue(dataOrException);
                });
        return mutableLiveData;
    }

}
