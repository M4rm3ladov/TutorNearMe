package com.ren.tutornearme.ui.home;

import static com.ren.tutornearme.util.Common.TUTOR_LOCATION_REFERENCE;
import static com.ren.tutornearme.util.Common.TUTOR_REQUEST_REFERENCE;
import static com.ren.tutornearme.util.Common.TUTOR_STUDENT_SESSION_REFERENCE;
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
import com.ren.tutornearme.model.StudentInfo;
import com.ren.tutornearme.model.TutorRequestInfo;
import com.ren.tutornearme.model.TutorSubject;

import java.util.HashMap;
import java.util.Map;


public class HomeRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseUser currentUser;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference onlineRef = db.getReference().child(".info/connected");
    private DatabaseReference tutorLocationRef;
    private DatabaseReference currentUserRef;

    private final DatabaseReference tutorRequestRef;
    private final DatabaseReference tutorWorkingRef;
    private final DatabaseReference tutorStudentSessionRef;
    private ValueEventListener tutorRequestRefListener = null;

    public HomeRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        tutorRequestRef = db.getReference(TUTOR_REQUEST_REFERENCE);
        tutorWorkingRef = db.getReference(TUTOR_WORKING_REFERENCE);
        tutorStudentSessionRef = db.getReference(TUTOR_STUDENT_SESSION_REFERENCE);
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    public void removeTutorRequestListener() {
        if (tutorRequestRefListener != null)
            tutorRequestRef.removeEventListener(tutorRequestRefListener);
    }

    public void removeTutorLocation() {
        if (tutorLocationRef == null) return;
        GeoFire geoFire = new GeoFire(tutorLocationRef);
        geoFire.removeLocation(getCurrentUser().getUid());
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> checkTutorLocationSet(LocationResult locationResult) {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

        GeoFire geoFire = new GeoFire(tutorLocationRef);
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

    public MutableLiveData<DataOrException<Boolean, Exception>> checkStudentRequest(Location location) {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

        if (getCurrentUser() != null) {
            tutorRequestRefListener = tutorRequestRef.child(getCurrentUser().getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                TutorRequestInfo tutorRequestInfo = snapshot.getValue(TutorRequestInfo.class);
                                dataOrException.data = tutorRequestInfo != null &&
                                        tutorRequestInfo.getStudentLocation() != null &&
                                        tutorRequestInfo.getSessionKey() != null &&
                                        tutorRequestInfo.getStudentInfo() != null &&
                                        tutorRequestInfo.getTutorSubject() != null;
                            } else
                                dataOrException.data = false;
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

    public MutableLiveData<DataOrException<Boolean, Exception>> updateTutorWorkingLocation(Location location) {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

        GeoFire geoFire = new GeoFire(tutorWorkingRef);
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

    public MutableLiveData<DataOrException<Boolean, Exception>> setTutorStudentSessionInfo
            (TutorSubject tutorSubject, StudentInfo studentInfo, double tutorFee, long sessionStart, String sessionKey) {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

        Map<String, Object> data = new HashMap<>();
        data.put("isOngoing", true);
        data.put("tutorSubject", tutorSubject);
       /* data.put("studentInfo", studentInfo);*/
        data.put("tutorFee", tutorFee);
        data.put("sessionStart", sessionStart);

        tutorStudentSessionRef.child(sessionKey).updateChildren(data)
                .addOnSuccessListener(unused -> {
                    dataOrException.data = true;
                    mutableLiveData.postValue(dataOrException);
                })
                .addOnFailureListener(e -> {
                    dataOrException.exception = e;
                    mutableLiveData.postValue(dataOrException);
                });
        return mutableLiveData;
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> setTutorStudentSessionLocation
            (Location studentLocation, Location tutorLocation, String refKey) {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

        GeoFire studentGeoFire = new GeoFire(tutorStudentSessionRef.child(refKey));
        studentGeoFire.setLocation("studentLocation",
                new GeoLocation(studentLocation.getLatitude(),
                        studentLocation.getLongitude()), (key, error) -> {
                    if (error != null) {
                        dataOrException.exception = error.toException();
                        mutableLiveData.postValue(dataOrException);
                    } else {
                        GeoFire tutorGeoFire = new GeoFire(tutorStudentSessionRef.child(refKey));
                        tutorGeoFire.setLocation("tutorLocation",
                                new GeoLocation(tutorLocation.getLatitude(), tutorLocation.getLongitude()),
                                (key1, error1) -> {
                                    if (error1 != null)
                                        dataOrException.exception = error1.toException();
                                    else
                                        dataOrException.data = true;
                                    mutableLiveData.postValue(dataOrException);
                                });
                    }
                });

        return mutableLiveData;
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> setTutorStudentSessionEnd
            (long sessionEnd, String refKey) {
        MutableLiveData<DataOrException<Boolean, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<Boolean, Exception> dataOrException = new DataOrException<>();

        Map<String, Object> data = new HashMap<>();
        data.put("sessionEnd", sessionEnd);
        data.put("isOngoing", false);

        tutorStudentSessionRef.child(refKey)
                .updateChildren(data).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        dataOrException.data = true;
                    else
                        dataOrException.exception = task.getException();
                    mutableLiveData.postValue(dataOrException);
                });
        return mutableLiveData;
    }

    public MutableLiveData<DataOrException<TutorRequestInfo, Exception>> getTutorRequestInfo() {
        MutableLiveData<DataOrException<TutorRequestInfo, Exception>> mutableLiveData = new MutableLiveData<>();
        DataOrException<TutorRequestInfo, Exception> dataOrException = new DataOrException<>();

        tutorRequestRef.child(getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        dataOrException.data = snapshot.getValue(TutorRequestInfo.class);
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

    public DatabaseReference getOnlineRef() { return onlineRef; }
    public DatabaseReference getCurrentUserRef() { return currentUserRef; }
    public DatabaseReference getTutorWorkingRef() { return tutorWorkingRef.child(getCurrentUser().getUid()); }
    public DatabaseReference getTutorRequestRef() { return tutorRequestRef.child(getCurrentUser().getUid()); }

    public void setCurrentTutorLocationRef(String cityName) {
        tutorLocationRef = db.getReference(TUTOR_LOCATION_REFERENCE).child(cityName);
        currentUserRef = tutorLocationRef.child(getCurrentUser().getUid());
    }

}
