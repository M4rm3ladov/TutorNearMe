package com.ren.tutornearme.register;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.TutorInfo;

public class RegisterViewModel extends ViewModel {
    private final RegisterRepository profileRepository;

    public RegisterViewModel() {
        this.profileRepository = new RegisterRepository();
    }

    public FirebaseUser getCurrentUser() {
        return profileRepository.getCurrentUser();
    }

    public MutableLiveData<DataOrException<TutorInfo, Exception>> registerTutor(TutorInfo tutorInfo) {
        return profileRepository.registerTutor(tutorInfo);
    }
}
