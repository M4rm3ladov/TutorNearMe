package com.ren.tutornearme.ui.subject.subject_list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.SubjectInfo;

import java.util.ArrayList;

public class SubjectListViewModel extends ViewModel {
    private final SubjectListRepository subjectListRepository;

    public SubjectListViewModel() {
        subjectListRepository = new SubjectListRepository();
    }

    public LiveData<DataOrException<ArrayList<SubjectInfo>, Exception>> getSubjectInfoList() {
        return subjectListRepository.getSubjectInfoList();
    }

    public void removeSubjectInfoListener() {
        subjectListRepository.removeSubjectInfoListener();
    }
}
