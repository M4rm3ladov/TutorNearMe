package com.ren.tutornearme.ui.profile;

import androidx.lifecycle.ViewModel;

public class ProfileViewModel extends ViewModel {

    private final ProfileRepository profileRepository;

    public ProfileViewModel() {
        this.profileRepository = new ProfileRepository();
    }


}