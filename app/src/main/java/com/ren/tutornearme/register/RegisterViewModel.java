package com.ren.tutornearme.register;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.TutorInfo;

public class RegisterViewModel extends ViewModel {
    private final RegisterRepository registerRepository;

    public RegisterViewModel() {
        this.registerRepository = new RegisterRepository();
    }

    public FirebaseUser getCurrentUser() {
        return registerRepository.getCurrentUser();
    }

    public void signOut() {
        registerRepository.signOut();
    }

    public MutableLiveData<DataOrException<TutorInfo, Exception>> registerTutor(TutorInfo tutorInfo) {
        return registerRepository.registerTutor(tutorInfo);
    }
}
