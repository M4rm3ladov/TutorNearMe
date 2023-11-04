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

    public MutableLiveData<DataOrException<TutorInfo, Exception>> getTutorInfoFromFirebase() {
        return sharedRepository.getTutorInfo();
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
