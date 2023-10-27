package com.ren.tutornearme.ui.profile;

import static com.ren.tutornearme.util.Common.TUTOR_INFO_REFERENCE;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class ProfileRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference tutorInfoRef = db.getReference(TUTOR_INFO_REFERENCE);

    public ProfileRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

}
