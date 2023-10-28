package com.ren.tutornearme.basic_info;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.TutorInfo;

public class BasicInfoViewModel extends ViewModel {
    private final BasicInfoRepository basicInfoRepository;

    public BasicInfoViewModel() {
        this.basicInfoRepository = new BasicInfoRepository();
    }

    public FirebaseUser getCurrentUser() {
        return basicInfoRepository.getCurrentUser();
    }

    public void signOut() {
        basicInfoRepository.signOut();
    }

    public MutableLiveData<DataOrException<TutorInfo, Exception>> saveTutorInfo(TutorInfo tutorInfo) {
        return basicInfoRepository.saveTutorInfo(tutorInfo);
    }
}
