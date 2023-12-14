package com.ren.tutornearme.ui.subject.subject_list;

import static com.ren.tutornearme.util.Common.SUBJECT_REFERENCE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.SubjectInfo;

import java.util.ArrayList;

public class SubjectListRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference subjectInfoRef = db.getReference(SUBJECT_REFERENCE);

    private ChildEventListener subjectInfoEventListener = null;

    public SubjectListRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public MutableLiveData<DataOrException<ArrayList<SubjectInfo>, Exception>> getSubjectInfoList() {
        MutableLiveData<DataOrException<ArrayList<SubjectInfo>, Exception>> mutableLiveData = new MutableLiveData<>();

        if (getCurrentUser() != null) {
            DataOrException<ArrayList<SubjectInfo>, Exception> dataOrException = new DataOrException<>();
            ArrayList<SubjectInfo> subjectInfoArrayList = new ArrayList<>();

            subjectInfoEventListener = subjectInfoRef.orderByChild("updatedDate").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.exists()) {
                        //subjectInfoArrayList.clear();
                        SubjectInfo subjectInfo = snapshot.getValue(SubjectInfo.class);
                        SubjectInfo subjectInfoClone = subjectInfo
                                .copyWith(snapshot.getKey(), null, null, null, null);

                        subjectInfoArrayList.add(subjectInfoClone);

                        dataOrException.data = subjectInfoArrayList;
                        mutableLiveData.postValue(dataOrException);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.exists()) {
                        SubjectInfo subjectInfo = snapshot.getValue(SubjectInfo.class);
                        SubjectInfo subjectInfoClone = subjectInfo
                                .copyWith(snapshot.getKey(), null, null, null, null);

                        int index = -1;
                        for (int i = 0; i < subjectInfoArrayList.size(); i++)
                            if (subjectInfoArrayList.get(i).getId().equals(snapshot.getKey()))
                                index = i;

                        subjectInfoArrayList.set(index, subjectInfoClone);

                        dataOrException.data = subjectInfoArrayList;
                        mutableLiveData.postValue(dataOrException);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int index = -1;
                        for (int i = 0; i < subjectInfoArrayList.size(); i++)
                            if (subjectInfoArrayList.get(i).getId().equals(snapshot.getKey()))
                                index = i;

                        subjectInfoArrayList.remove(index);

                        dataOrException.data = subjectInfoArrayList;
                        mutableLiveData.postValue(dataOrException);
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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

    public void removeSubjectInfoListener() {
        if (subjectInfoEventListener != null)
            subjectInfoRef.removeEventListener(subjectInfoEventListener);
    }
}
