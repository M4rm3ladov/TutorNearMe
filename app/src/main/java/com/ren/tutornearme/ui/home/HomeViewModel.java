package com.ren.tutornearme.ui.home;

import android.graphics.drawable.Drawable;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.DatabaseReference;
import com.ren.tutornearme.data.DataOrException;

public class HomeViewModel extends ViewModel {
    private final HomeRepository homeRepository;
    private Drawable locationButtonImage;
    private final MutableLiveData<Boolean> isTutorWorking = new MutableLiveData<>();

    public HomeViewModel() {
        this.homeRepository = new HomeRepository();
    }

    public MutableLiveData<Boolean> getIsTutorWorking() {
        return isTutorWorking;
    }

    public void setIsTutorWorking(Boolean isTutorWorking) {
        this.isTutorWorking.postValue(isTutorWorking);
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

    public LiveData<DataOrException<Boolean, Exception>> isTutorLocationSet(Location location) {
        return homeRepository.checkTutorLocationSet(location);
    }

    public LiveData<DataOrException<Boolean, Exception>> removeTutorWorking() {
        return homeRepository.removeTutorWorking();
    }

    public LiveData<DataOrException<Boolean, Exception>> removeTutorRequest() {
        return homeRepository.removeTutorRequest();
    }

    public LiveData<DataOrException<Boolean, Exception>> checkStudentRequest(Location location) {
        return homeRepository.checkStudentRequest(location);
    }

    public LiveData<DataOrException<Boolean, Exception>> updateTutorWorkingLocation(Location location) {
        return homeRepository.updateTutorWorkingLocation(location);
    }

    public DatabaseReference getCurrentUserRef() { return homeRepository.getCurrentUserRef(); }

    public DatabaseReference getTutorWorkingRef() { return homeRepository.getTutorWorkingRef(); }
    public DatabaseReference getTutorRequestRef() { return homeRepository.getTutorRequestRef(); }

    public void setTutorLocationRef(String cityName) {
        homeRepository.setCurrentTutorLocationRef(cityName);
    }

    public DatabaseReference getOnlineRef() { return homeRepository.getOnlineRef(); }

}