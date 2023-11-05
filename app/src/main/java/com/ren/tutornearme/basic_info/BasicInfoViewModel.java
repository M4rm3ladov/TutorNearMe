package com.ren.tutornearme.basic_info;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.TutorInfo;

import java.util.Map;

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

    public LiveData<DataOrException<TutorInfo, Exception>> saveTutorInfo(TutorInfo tutorInfo) {
        return basicInfoRepository.saveTutorInfo(tutorInfo);
    }

    public LiveData<DataOrException<TutorInfo, Exception>> updateTutorInfo
            (Map<String, Object> tutorInfo) {
        return basicInfoRepository.updateTutorInfo(tutorInfo);
    }
}
