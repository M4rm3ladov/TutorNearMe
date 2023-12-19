package com.ren.tutornearme.ui.subject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.SubjectInfo;
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
            (TutorSubject tutorSubject, SubjectInfo subjectInfo, TutorInfo tutorInfo) {
        return subjectSharedRepository.saveTutorSubject(tutorSubject, subjectInfo, tutorInfo);
    }
}