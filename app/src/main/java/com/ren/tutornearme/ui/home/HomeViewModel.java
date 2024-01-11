package com.ren.tutornearme.ui.home;

import android.graphics.drawable.Drawable;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.DatabaseReference;
import com.ren.tutornearme.data.DataOrException;

public class HomeViewModel extends ViewModel {
    private final HomeRepository homeRepository;
    private Drawable locationButtonImage;

    public HomeViewModel() {
        this.homeRepository = new HomeRepository();
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

    public LiveData<DataOrException<Boolean, Exception>> isTutorLocationSet(LocationResult locationResult) {
        return homeRepository.checkTutorLocationSet(locationResult);
    }

    public LiveData<DataOrException<Boolean, Exception>> isTutorLocationSet(Location location) {
        return homeRepository.checkTutorLocationSet(location);
    }

    public DatabaseReference getCurrentUserRef() { return homeRepository.getCurrentUserRef(); }

    public void setTutorLocationRef(String cityName) {
        homeRepository.setCurrentTutorLocationRef(cityName);
    }

    public DatabaseReference getOnlineRef() { return homeRepository.getOnlineRef(); }

}