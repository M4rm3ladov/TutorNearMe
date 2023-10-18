package com.ren.tutornearme.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.ren.tutornearme.data.DataOrException;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;

    public AuthViewModel() {
        this.authRepository = new AuthRepository();
    }

    public LiveData<Boolean> checkIfSignedIn() {
        return authRepository.checkIfSignedIn();
    }

    public LiveData<DataOrException<Boolean, Exception>> checkIfRegistered() {
        return authRepository.checkIfRegistered();
    }

}
