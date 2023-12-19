package com.ren.tutornearme.ui.subject.subject_file;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.ren.tutornearme.data.DataOrException;

import java.util.Map;

public class SubjectFilesViewModel extends ViewModel {
    private final  SubjectFilesRepository subjectFilesRepository;
    public SubjectFilesViewModel() {
        this.subjectFilesRepository = new SubjectFilesRepository();
    }
    public LiveData<DataOrException<Map<String, Object>, Exception>> uploadCredential(Uri pdfUri) {
        return subjectFilesRepository.uploadCredential(pdfUri);
    }

    public LiveData<DataOrException<Boolean, Exception>> updateCredential(String key) {
        return subjectFilesRepository.updateCredential(key);
    }
}
