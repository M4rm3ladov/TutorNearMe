package com.ren.tutornearme.ui.home;

import static com.ren.tutornearme.util.Common.TUTOR_LOCATION_REFERENCE;
import static com.ren.tutornearme.util.Common.TUTOR_REQUEST_REFERENCE;
import static com.ren.tutornearme.util.Common.TUTOR_WORKING_REFERENCE;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.StudentGeo;
import com.ren.tutornearme.model.StudentInfo;
import com.ren.tutornearme.model.TutorSubject;


public class HomeRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseUser currentUser;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference onlineRef = db.getReference().child(".info/connected");
    private DatabaseReference tutorLocationRef;
    private DatabaseReference currentUserRef;
    private GeoFire geoFire;

    private final DatabaseReference tutorRequestRef;
    private final DatabaseReference tutorWorkingRef;
    private ValueEventListener tutorRequestRefListener = null;


    public HomeRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        geoFire = new GeoFire(db.getReference(TUTOR_LOCATION_REFERENCE));
        tutorRequestRef = db.getReference(TUTOR_REQUEST_REFERENCE);
        tutorWorkingRef = db.getReference(TUTOR_WORKING_REFERENCE);
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    public void removeTutorRequestListener() {
        if (tutorRequestRefListener != null)
            tutorRequestRef.removeEventListener(tutorRequestRefListener);
    }

    public void removeTutorLocation() {
        GeoFire geoFire = new GeoFire(tutorLocationRef);
        geoFire.removeLocation(getCurrentUser().getUid());
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> checkTutorLocationSet(LocationResult locationResult) {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

        geoFire = new GeoFire(tutorLocationRef);
        geoFire.setLocation(currentUser.getUid(),
                new GeoLocation(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude()),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error != null)
                            dataOrException.exception = error.toException();
                        else
                            dataOrException.data = true;

                        mutableLiveData.postValue(dataOrException);
                    }
                });
        return mutableLiveData;
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> checkTutorLocationSet(Location location) {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

        geoFire = new GeoFire(tutorLocationRef);
        geoFire.setLocation(currentUser.getUid(),
                new GeoLocation(location.getLatitude(),
                        location.getLongitude()),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error != null)
                            dataOrException.exception = error.toException();
                        else
                            dataOrException.data = true;

                        mutableLiveData.postValue(dataOrException);
                    }
                });
        return mutableLiveData;
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> removeTutorWorking() {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

        if (getCurrentUser() != null) {
            tutorWorkingRef.child(getCurrentUser().getUid()).removeValue()
                    .addOnCompleteListener(task -> {
                        dataOrException.data = task.isSuccessful();
                        mutableLiveData.postValue(dataOrException);
                    }).addOnFailureListener(e -> {
                        dataOrException.exception = e;
                        mutableLiveData.postValue(dataOrException);
                    });
        }
        return mutableLiveData;
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> removeTutorRequest() {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

        if (getCurrentUser() != null) {
            tutorRequestRef.child(getCurrentUser().getUid()).removeValue()
                    .addOnCompleteListener(task -> {
                        dataOrException.data = task.isSuccessful();
                        mutableLiveData.postValue(dataOrException);
                    }).addOnFailureListener(e -> {
                        dataOrException.exception = e;
                        mutableLiveData.postValue(dataOrException);
                    });
        }
        return mutableLiveData;
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> checkStudentRequest(Location location) {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

        if (getCurrentUser() != null) {
            tutorRequestRefListener = tutorRequestRef.child(getCurrentUser().getUid())
                    .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        dataOrException.data = true;
                        mutableLiveData.postValue(dataOrException);
                    } else {
                        dataOrException.data = false;
                        mutableLiveData.postValue(dataOrException);
                    }
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

    public MutableLiveData<DataOrException<StudentGeo, Exception>> getStudentGeo() {
        MutableLiveData<DataOrException<StudentGeo, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<StudentGeo, Exception> dataOrException = new DataOrException<>();

        tutorRequestRef.child(getCurrentUser().getUid()).child("studentLocation")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        dataOrException.data = snapshot.getValue(StudentGeo.class);
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

    public MutableLiveData<DataOrException<TutorSubject, Exception>> getStudentTutorSubject() {
        MutableLiveData<DataOrException<TutorSubject, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<TutorSubject, Exception> dataOrException = new DataOrException<>();

        tutorRequestRef.child(getCurrentUser().getUid()).child("tutorSubject")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        dataOrException.data = snapshot.getValue(TutorSubject.class);
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

    public MutableLiveData<DataOrException<StudentInfo, Exception>> getStudentInfo() {
        MutableLiveData<DataOrException<StudentInfo, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<StudentInfo, Exception> dataOrException = new DataOrException<>();

        tutorRequestRef.child(getCurrentUser().getUid()).child("studentInfo")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        dataOrException.data = snapshot.getValue(StudentInfo.class);
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

    public MutableLiveData<DataOrException<Boolean, Exception>> updateTutorWorkingLocation(Location location) {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

        geoFire = new GeoFire(tutorWorkingRef);
            geoFire.setLocation(currentUser.getUid(),
                    new GeoLocation(location.getLatitude(),
                            location.getLongitude()), (key, error) -> {
                if (error != null)
                    dataOrException.exception = error.toException();
                else
                    dataOrException.data = true;
                mutableLiveData.postValue(dataOrException);

        });
            return mutableLiveData;
    }

    public DatabaseReference getOnlineRef() { return onlineRef; }

    public DatabaseReference getCurrentUserRef() { return currentUserRef; }
    public DatabaseReference getTutorWorkingRef() { return tutorWorkingRef.child(getCurrentUser().getUid()); }
    public DatabaseReference getTutorRequestRef() { return tutorRequestRef.child(getCurrentUser().getUid()); }

    public void setCurrentTutorLocationRef(String cityName) {
        tutorLocationRef = db.getReference(TUTOR_LOCATION_REFERENCE).child(cityName);
        currentUserRef = tutorLocationRef.child(getCurrentUser().getUid());
    }

}
