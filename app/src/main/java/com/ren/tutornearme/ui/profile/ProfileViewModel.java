package com.ren.tutornearme.ui.profile;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ren.tutornearme.data.DataOrException;

import java.util.Map;

public class ProfileViewModel extends ViewModel {

    private final ProfileRepository profileRepository;
    private final MutableLiveData<Uri> validIdUri = new MutableLiveData<>();
    private final MutableLiveData<String> validIdPath = new MutableLiveData<>();
    private final MutableLiveData<Uri> resumeUri = new MutableLiveData<>();
    private final MutableLiveData<String> resumePath = new MutableLiveData<>();

    public ProfileViewModel() {
        this.profileRepository = new ProfileRepository();
    }

    public LiveData<DataOrException<Map<String, Object>, Exception>> uploadAvatarOrValidId
            (Uri imageUri, String imageFlag) {
        return profileRepository.uploadAvatarOrValidId(imageUri, imageFlag);
    }

    public LiveData<DataOrException<Boolean, Exception>> updateAvatarOrValidId(String imageFlag, String validIdType) {
        return profileRepository.updateAvatarOrValidId(imageFlag, validIdType);
    }

    public LiveData<DataOrException<Map<String, Object>, Exception>> uploadResume(Uri pdfUri) {
        return profileRepository.uploadResume(pdfUri);
    }

    public LiveData<DataOrException<Boolean, Exception>> updateResume() {
        return profileRepository.updateResume();
    }


    public MutableLiveData<String> getValidIdPath() {
        return validIdPath;
    }

    public void setValidIdPath(String path) {
        validIdPath.setValue(path);
    }

    public void setValidIdUri(Uri uri) {
        validIdUri.setValue(uri);
    }

    public MutableLiveData<Uri> getValidIdUri() {
        return validIdUri;
    }


    public MutableLiveData<String> getResumePath() {
        return resumePath;
    }

    public void setResumePath(String path) {
        resumePath.setValue(path);
    }

    public void setResumeUri(Uri uri) {
        resumeUri.setValue(uri);
    }

    public MutableLiveData<Uri> getResumeUri() {
        return resumeUri;
    }
}