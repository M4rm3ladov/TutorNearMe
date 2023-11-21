package com.ren.tutornearme;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.TutorInfo;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<TutorInfo> tutorInfoLiveData;
    private final SharedRepository sharedRepository;
    private TutorInfo tutorInfo;

    public SharedViewModel() {
        sharedRepository = new SharedRepository();
        tutorInfoLiveData = new MutableLiveData<>();
    }

    public LiveData<DataOrException<TutorInfo, Exception>> getTutorInfoFromFirebase() {
        return sharedRepository.getTutorInfo();
    }

    public void setTutorInfoListener() {
        sharedRepository.setTutorInfoListener();
    }

    public void removeTutorInfoListener() {
        sharedRepository.removeTutorInfoListener();
    }

    public LiveData<DataOrException<String, Exception>> getTutorAccountStatus() {
        return sharedRepository.getTutorAccountStatus();
    }

    public void setTutorAccountStatusListener() {
        sharedRepository.setTutorAccountStatusListener();
    }

    public void removeTutorAccountStatusListener() {
        sharedRepository.removeTutorAccountStatusListener();
    }

    public void setTutorInfo(TutorInfo tutorInfo) {
        tutorInfoLiveData.setValue(tutorInfo);
        this.tutorInfo = tutorInfo;
    }

    public LiveData<TutorInfo> getTutorInfoLiveData() {
        return tutorInfoLiveData;
    }
    public TutorInfo getTutorInfo() {
        return tutorInfo;
    }
}
