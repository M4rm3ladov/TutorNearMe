package com.ren.tutornearme.ui.profile;

import static com.ren.tutornearme.util.Common.BARANGAY;
import static com.ren.tutornearme.util.Common.FIRST_NAME;
import static com.ren.tutornearme.util.Common.GENDER;
import static com.ren.tutornearme.util.Common.LAST_NAME;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.ren.tutornearme.R;
import com.ren.tutornearme.SharedViewModel;
import com.ren.tutornearme.model.TutorInfo;
import com.ren.tutornearme.register.RegisterActivity;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    private CardView basicInfoCardView;
    private Button uploadResumeButton, uploadValidIDButton;
    private TextView tutorName, tutorGender, tutorBarangay;
    protected TutorInfo mTutorInfo;
    private Resources res;
    private ProfileViewModel profileViewModel;
    private SharedViewModel sharedViewModel;
    private FragmentActivity mActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        if (context instanceof Activity){
            mActivity = (FragmentActivity) context;
        }
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewModels();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initBindViews(view);
        initPopulateBasicInfo();
        initBindListeners();

        return view;
    }

    private void initViewModels() {
        profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        sharedViewModel = new ViewModelProvider(mActivity).get(SharedViewModel.class);
    }

    private void initBindListeners() {
        basicInfoCardView.setOnClickListener(this);
        uploadValidIDButton.setOnClickListener(this);
        uploadResumeButton.setOnClickListener(this);
    }

    private void initBindViews(View view) {
        res = getResources();

        basicInfoCardView = view.findViewById(R.id.profile_basic_info_cardview);
        uploadValidIDButton = view.findViewById(R.id.profile_upload_id_button);
        uploadResumeButton = view.findViewById(R.id.profile_upload_resume_button);

        tutorName = view.findViewById(R.id.profile_tutor_name_textview);
        tutorGender = view.findViewById(R.id.profile_tutor_gender_textview);
        tutorBarangay = view.findViewById(R.id.profile_tutor_brgy_textview);
    }

    private void initPopulateBasicInfo() {
        sharedViewModel.getTutorInfoLiveData().observe(mActivity, new Observer<TutorInfo>() {
            @Override
            public void onChanged(TutorInfo tutorInfo) {
                String name = String.format(res.getString(R.string.tutor_name),
                        tutorInfo.getFirstName(), tutorInfo.getLastName());
                tutorName.setText(name);
                tutorGender.setText(tutorInfo.getGender());
                tutorBarangay.setText(tutorInfo.getAddress());
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.profile_basic_info_cardview) {
            Intent intent = new Intent(getActivity(), RegisterActivity.class);
            Bundle bundle = new Bundle();

            mTutorInfo = sharedViewModel.getTutorInfo();

            bundle.putString(FIRST_NAME, mTutorInfo.getFirstName());
            bundle.putString(LAST_NAME, mTutorInfo.getLastName());
            bundle.putString(GENDER, mTutorInfo.getGender());
            bundle.putString(BARANGAY, mTutorInfo.getAddress());

            intent.putExtras(bundle);
            startActivity(intent);
        }
    }
}