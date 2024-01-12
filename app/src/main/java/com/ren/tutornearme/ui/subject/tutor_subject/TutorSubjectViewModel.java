package com.ren.tutornearme.ui.subject.tutor_subject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.TutorSubject;

import java.util.ArrayList;

public class TutorSubjectViewModel extends ViewModel {

    private final TutorSubjectRepository tutorSubjectRepository;

    public TutorSubjectViewModel() {
        tutorSubjectRepository = new TutorSubjectRepository();
    }

    public LiveData<DataOrException<ArrayList<TutorSubject>, Exception>> getTutorSubjectList() {
        return tutorSubjectRepository.getTutorSubjectList();
    }

    public LiveData<DataOrException<Boolean, Exception>> setTutorSubjectSession(String key, int sessionLength) {
        return tutorSubjectRepository.setTutorSubjectSession(key, sessionLength);
    }

    public LiveData<DataOrException<Boolean, Exception>> setTutorSubjectAvailability(String key, boolean isAvailable) {
        return tutorSubjectRepository.setTutorSubjectAvailability(key, isAvailable);
    }

    public void removeTutorSubjectListener () {
        tutorSubjectRepository.removeTutorSubjectListener();
    }
}
