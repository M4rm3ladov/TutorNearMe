package com.ren.tutornearme.ui.subject.tutor_subject;

import static com.ren.tutornearme.util.Common.TUTOR_SUBJECT_REFERENCE;

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
import com.ren.tutornearme.model.TutorSubject;

import java.util.ArrayList;

public class TutorSubjectRepository {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference tutorSubjectRef = db.getReference(TUTOR_SUBJECT_REFERENCE);
    private ChildEventListener tutorSubjectEventListener = null;

    public TutorSubjectRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    private FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public MutableLiveData<DataOrException<ArrayList<TutorSubject>, Exception>> getTutorSubjectList() {
        MutableLiveData<DataOrException<ArrayList<TutorSubject>, Exception>> mutableLiveData = new MutableLiveData<>();

        if (getCurrentUser() != null) {
            DataOrException<ArrayList<TutorSubject>, Exception> dataOrException = new DataOrException<>();
            ArrayList<TutorSubject> tutorSubjects = new ArrayList<>();

            tutorSubjectEventListener = tutorSubjectRef
                    .orderByChild("tutorInfo/uid").equalTo(getCurrentUser().getUid())
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            if (snapshot.exists()) {

                                TutorSubject tutorSubject = snapshot.getValue(TutorSubject.class);
                                TutorSubject tutorSubjectClone = tutorSubject
                                        .copyWith(snapshot.getKey(), null, null, null,
                                                null, null, null);

                                tutorSubjects.add(tutorSubjectClone);

                                dataOrException.data = tutorSubjects;
                                mutableLiveData.postValue(dataOrException);
                            }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            if (snapshot.exists()) {
                                TutorSubject tutorSubject = snapshot.getValue(TutorSubject.class);
                                TutorSubject tutorSubjectClone = tutorSubject
                                        .copyWith(snapshot.getKey(), null, null, null,
                                                null, null, null);

                                int index = -1;
                                for (int i = 0; i < tutorSubjects.size(); i++)
                                    if (tutorSubjects.get(i).getId().equals(snapshot.getKey()))
                                        index = i;

                                if (index != -1)
                                    tutorSubjects.set(index, tutorSubjectClone);

                                dataOrException.data = tutorSubjects;
                                mutableLiveData.postValue(dataOrException);
                            }
                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                int index = -1;
                                for (int i = 0; i < tutorSubjects.size(); i++)
                                    if (tutorSubjects.get(i).getId().equals(snapshot.getKey()))
                                        index = i;

                                tutorSubjects.remove(index);

                                dataOrException.data = tutorSubjects;
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

    public void removeTutorSubjectListener() {
        if (tutorSubjectEventListener != null)
            tutorSubjectRef.removeEventListener(tutorSubjectEventListener);
    }
}
