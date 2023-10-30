package com.ren.tutornearme.ui.profile;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.ren.tutornearme.data.DataOrException;

import java.util.Map;

public class ProfileViewModel extends ViewModel {

    private final ProfileRepository profileRepository;

    public ProfileViewModel() {
        this.profileRepository = new ProfileRepository();
    }

    public LiveData<DataOrException<Map<String, Object>, Exception>> uploadAvatar(Uri imageUri) {
        return profileRepository.uploadAvatar(imageUri);
    }

    public LiveData<DataOrException<Boolean, Exception>> updateAvatar() {
        return profileRepository.updateAvatar();
    }
}