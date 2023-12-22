package com.ren.tutornearme.ui.subject;

import static com.ren.tutornearme.util.Common.TUTOR_INFO_REFERENCE;
import static com.ren.tutornearme.util.Common.TUTOR_SUBJECT_LOOKUP_REFERENCE;
import static com.ren.tutornearme.util.Common.TUTOR_SUBJECT_REFERENCE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.SubjectInfo;
import com.ren.tutornearme.model.TutorInfo;
import com.ren.tutornearme.model.TutorSubject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SubjectSharedRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference tutorSubjectRef = db.getReference(TUTOR_SUBJECT_REFERENCE);
    private final DatabaseReference tutorSubjectLookUpRef = db.getReference(TUTOR_SUBJECT_LOOKUP_REFERENCE);
    private final DatabaseReference tutorInfoRef = db.getReference(TUTOR_INFO_REFERENCE);
    public SubjectSharedRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public MutableLiveData<DataOrException<String, Exception>> saveTutorSubject
            (TutorSubject tutorSubject, SubjectInfo subjectInfo, TutorInfo tutorInfo) {
        MutableLiveData<DataOrException<String, Exception>> mutableLiveData = new MutableLiveData<>();

        if (getCurrentUser() != null) {
            DataOrException<String, Exception> dataOrException = new DataOrException<>();

            tutorSubjectRef.push().setValue(tutorSubject
                    , new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    if (error != null)
                        dataOrException.exception = error.toException();
                    else {
                        if(ref.getKey() == null) {
                            dataOrException.data = "";
                        }
                        tutorSubjectRef.child(ref.getKey()).child(subjectInfo.getId()).setValue(subjectInfo)
                                .addOnCompleteListener(
                                new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        tutorSubjectRef.child(ref.getKey()).child(getCurrentUser().getUid()).setValue(tutorInfo)
                                                .addOnCompleteListener(
                                                new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        dataOrException.data = ref.getKey();
                                                        mutableLiveData.postValue(dataOrException);
                                                    }
                                                }
                                        ).addOnFailureListener(e -> {
                                            dataOrException.exception = e;
                                            mutableLiveData.postValue(dataOrException);
                                        });
                                    }
                                }
                        ).addOnFailureListener(e -> {
                            dataOrException.exception = e;
                            mutableLiveData.postValue(dataOrException);
                        });
                    }
                }
            });
        }
        return mutableLiveData;
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> saveTutorSubjectLookUp (String subjectId) {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();

        if (getCurrentUser() != null) {
            DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

            Map<String, String> postValues = new HashMap<>();
            postValues.put("subjectId", subjectId);
            postValues.put("tutorId", getCurrentUser().getUid());

            tutorSubjectLookUpRef.push().setValue(postValues)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            dataOrException.data = task.isSuccessful();
                            mutableLiveData.postValue(dataOrException);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dataOrException.exception = e;
                            mutableLiveData.postValue(dataOrException);
                        }
                    });

        }
        return mutableLiveData;
    }

    public MutableLiveData<DataOrException<TutorInfo, Exception>> getTutorInfo() {
        MutableLiveData<DataOrException<TutorInfo, Exception>> mutableLiveData = new MutableLiveData<>();

        if (getCurrentUser() != null) {
            DataOrException<TutorInfo, Exception> dataOrException = new DataOrException<>();

            tutorInfoRef.child(getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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

    public MutableLiveData<DataOrException<Boolean, Exception>> checkIfDuplicateRequest(String subjectId) {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();

        if (getCurrentUser() != null) {
            DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

            tutorSubjectLookUpRef.orderByChild("tutorId").equalTo(getCurrentUser().getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            dataOrException.data = false;
                            for(DataSnapshot data: snapshot.getChildren()) {
                                if (Objects.equals(data.child("subjectId").getValue(String.class), subjectId)) {
                                    dataOrException.data = true;
                                    break;
                                }
                            }
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
