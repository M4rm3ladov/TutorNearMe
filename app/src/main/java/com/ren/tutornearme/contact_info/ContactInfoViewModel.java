package com.ren.tutornearme.contact_info;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ren.tutornearme.data.DataOrException;

public class ContactInfoViewModel extends ViewModel {
    private final ContactInfoRepository contactInfoRepository;
    public ContactInfoViewModel() {
        contactInfoRepository = new ContactInfoRepository();
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> checkIfEmailExists(String email) {
        return contactInfoRepository.checkIfEmailExists(email);
    }

    public MutableLiveData<DataOrException<Boolean, Exception>> updateEmail(String email) {
        return contactInfoRepository.updateEmail(email);
    }
}
