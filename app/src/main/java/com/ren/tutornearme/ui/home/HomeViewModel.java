package com.ren.tutornearme.ui.home;

import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private final HomeRepository homeRepository;

    public HomeViewModel() {
        this.homeRepository = new HomeRepository();
    }


}