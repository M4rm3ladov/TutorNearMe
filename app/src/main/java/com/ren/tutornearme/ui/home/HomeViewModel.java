package com.ren.tutornearme.ui.home;

import android.graphics.drawable.Drawable;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.DatabaseReference;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.StudentInfo;
import com.ren.tutornearme.model.TutorRequestInfo;
import com.ren.tutornearme.model.TutorSubject;

public class HomeViewModel extends ViewModel {
    private final HomeRepository homeRepository;
    private Drawable locationButtonImage;
    private final MutableLiveData<Boolean> isTutorBooked = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isTutorOnline = new MutableLiveData<>(false);
    private TutorRequestInfo mTutorRequestInfo;
    private Location mTutorSessionStartLocation;
    private double mTutorFee;

    public HomeViewModel() {
        this.homeRepository = new HomeRepository();
    }

    public TutorRequestInfo getmTutorRequestInfo() {
        return mTutorRequestInfo;
    }

    public void setmTutorRequestInfo(TutorRequestInfo mTutorRequestInfo) {
        this.mTutorRequestInfo = mTutorRequestInfo;
    }

    public MutableLiveData<Boolean> getIsTutorBooked() {
        return isTutorBooked;
    }

    public void setIsTutorOnline(Boolean isTutorOnline) {
        this.isTutorOnline.postValue(isTutorOnline);
    }

    public MutableLiveData<Boolean> getIsTutorOnline() {
        return isTutorOnline;
    }

    public double getmTutorFee() {
        return mTutorFee;
    }

    public void setmTutorFee(double mTutorFee) {
        this.mTutorFee = mTutorFee;
    }

    public Location getmTutorSessionStartLocation() {
        return mTutorSessionStartLocation;
    }

    public void setmTutorSessionStartLocation(Location mtutorSessionStartLocation) {
        this.mTutorSessionStartLocation = mtutorSessionStartLocation;
    }

    public void setIsTutorBooked(Boolean isTutorBooked) {
        this.isTutorBooked.postValue(isTutorBooked);
    }

    public void setLocationButtonImage(Drawable locationButtonImage) {
        this.locationButtonImage = locationButtonImage;
    }

    public Drawable getLocationButtonImage() {
        return locationButtonImage;
    }

    public void removeTutorLocation() {
        homeRepository.removeTutorLocation();
    }

    public void removeCustomerRequestListener() {
        homeRepository.removeTutorRequestListener();
    }

    public LiveData<DataOrException<Boolean, Exception>> isTutorLocationSet(LocationResult locationResult) {
        return homeRepository.checkTutorLocationSet(locationResult);
    }

    public LiveData<DataOrException<Boolean, Exception>> checkStudentRequest(Location location) {
        return homeRepository.checkStudentRequest(location);
    }

    public LiveData<DataOrException<Boolean, Exception>> updateTutorWorkingLocation(Location location) {
        return homeRepository.updateTutorWorkingLocation(location);
    }

    public LiveData<DataOrException<Boolean, Exception>> setTutorStudentSessionInfo
            (TutorSubject tutorSubject,
             StudentInfo studentInfo, double tutorFee, long sessionStart, String sessionKey) {
        return homeRepository.setTutorStudentSessionInfo
                (tutorSubject, studentInfo, tutorFee, sessionStart, sessionKey);
    }

    public LiveData<DataOrException<Boolean, Exception>> setTutorStudentSessionLocation
            (Location studentLocation, Location tutorLocation, String refKey) {
        return homeRepository.setTutorStudentSessionLocation(studentLocation, tutorLocation, refKey);
    }

    public LiveData<DataOrException<Boolean, Exception>> setTutorStudentSessionEnd
            (long sessionEnd, String refKey) {
        return homeRepository.setTutorStudentSessionEnd(sessionEnd, refKey);
    }

    public LiveData<DataOrException<TutorRequestInfo, Exception>> getTutorRequestInfo() {
        return homeRepository.getTutorRequestInfo();
    }

    public LiveData<DataOrException<Boolean, Exception>> removeTutorWorking() {
        return homeRepository.removeTutorWorking();
    }

    public LiveData<DataOrException<Boolean, Exception>> removeTutorRequest() {
        return homeRepository.removeTutorRequest();
    }

    public DatabaseReference getCurrentUserRef() { return homeRepository.getCurrentUserRef(); }

    public DatabaseReference getTutorWorkingRef() { return homeRepository.getTutorWorkingRef(); }
    public DatabaseReference getTutorRequestRef() { return homeRepository.getTutorRequestRef(); }

    public void setTutorLocationRef(String cityName) {
        homeRepository.setCurrentTutorLocationRef(cityName);
    }

    public DatabaseReference getOnlineRef() { return homeRepository.getOnlineRef(); }

}