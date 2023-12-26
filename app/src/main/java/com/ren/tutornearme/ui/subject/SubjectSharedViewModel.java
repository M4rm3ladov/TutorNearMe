package com.ren.tutornearme.ui.subject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.TutorInfo;
import com.ren.tutornearme.model.TutorSubject;

public class SubjectSharedViewModel extends ViewModel {
    private final SubjectSharedRepository subjectSharedRepository;

    public SubjectSharedViewModel() {
        subjectSharedRepository = new SubjectSharedRepository();
    }

    public LiveData<DataOrException<TutorInfo, Exception>> getTutorInfo() {
        return subjectSharedRepository.getTutorInfo();
    }

    public LiveData<DataOrException<String, Exception>> saveTutorSubject
            (TutorSubject tutorSubject) {
        return subjectSharedRepository.saveTutorSubject(tutorSubject);
    }

    public LiveData<DataOrException<Boolean, Exception>> saveTutorSubjectLookUp (String subjectInfoId) {
        return subjectSharedRepository.saveTutorSubjectLookUp(subjectInfoId);
    }

    public LiveData<DataOrException<Boolean, Exception>> checkIfDuplicateRequest(String subjectId) {
        return subjectSharedRepository.checkIfDuplicateRequest(subjectId);
    }
}