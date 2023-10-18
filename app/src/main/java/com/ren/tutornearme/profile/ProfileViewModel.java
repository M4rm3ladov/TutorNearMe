package com.ren.tutornearme.profile;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.TutorInfo;

public class ProfileViewModel extends ViewModel {
    private final ProfileRepository profileRepository;

    public ProfileViewModel() {
        this.profileRepository = new ProfileRepository();
    }

    public FirebaseUser getCurrentUser() {
        return profileRepository.getCurrentUser();
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> registerTutor(TutorInfo tutorInfo) {
        return profileRepository.registerTutor(tutorInfo);
    }
}
