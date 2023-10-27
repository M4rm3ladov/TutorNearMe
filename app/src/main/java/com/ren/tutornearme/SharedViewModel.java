package com.ren.tutornearme;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ren.tutornearme.model.TutorInfo;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<TutorInfo> tutorInfoLiveData;
    private TutorInfo tutorInfo;

    public SharedViewModel() {
        tutorInfoLiveData = new MutableLiveData<>();
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
