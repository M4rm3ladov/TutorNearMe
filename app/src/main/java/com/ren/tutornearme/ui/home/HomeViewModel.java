package com.ren.tutornearme.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.DatabaseReference;
import com.ren.tutornearme.data.DataOrException;

public class HomeViewModel extends ViewModel {
    private final HomeRepository homeRepository;

    public HomeViewModel() {
        this.homeRepository = new HomeRepository();
    }

    public void removeTutorLocation() {
        homeRepository.removeTutorLocation();
    }

    public LiveData<DataOrException<Boolean, Exception>> isTutorLocationSet(LocationResult locationResult) {
        return homeRepository.checkTutorLocationSet(locationResult);
    }

    public DatabaseReference getCurrentUserRef() { return homeRepository.getCurrentUserRef(); }

    public DatabaseReference getOnlineRef() { return homeRepository.getOnlineRef(); }

}