package com.ren.tutornearme.ui.profile;

import static com.ren.tutornearme.util.Common.BARANGAY;
import static com.ren.tutornearme.util.Common.FIRST_NAME;
import static com.ren.tutornearme.util.Common.GENDER;
import static com.ren.tutornearme.util.Common.LAST_NAME;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.ren.tutornearme.BuildConfig;
import com.ren.tutornearme.R;
import com.ren.tutornearme.SharedViewModel;
import com.ren.tutornearme.model.TutorInfo;
import com.ren.tutornearme.basic_info.BasicInfoActivity;
import com.ren.tutornearme.util.SnackBarHelper;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    private CardView basicInfoCardView;
    private Button uploadResumeButton, uploadValidIDButton;
    private TextView tutorName, tutorGender, tutorBarangay;
    private ImageView tutorAvatarImageView;
    protected TutorInfo mTutorInfo;
    private Resources res;
    private ProfileViewModel profileViewModel;
    private SharedViewModel sharedViewModel;
    private FragmentActivity mActivity;
    private View mView;

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
        mView = view;

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
        tutorAvatarImageView.setOnClickListener(this);
    }

    private void initBindViews(View view) {
        res = getResources();

        basicInfoCardView = view.findViewById(R.id.profile_basic_info_cardview);
        tutorAvatarImageView = view.findViewById(R.id.profile_img_imageview);
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

                if (!tutorInfo.getAvatar().isEmpty())
                    Glide.with((Context) mActivity)
                            .load(tutorInfo.getAvatar())
                            .into(tutorAvatarImageView);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.profile_basic_info_cardview) {
            navigateToBasicInfo();
        } else if (view.getId() == R.id.profile_img_imageview) {
            promptFilePermission();
        }
    }

    /*private boolean checkFilePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(mActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                // requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                showImagePicker();
            }
            return false;
        }
        return true;
    }*/

    private void checkFilePermissionForOldAPI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(mActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                // requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                showImagePicker();
            }
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {

                @Override
                public void onActivityResult(Boolean result) {
                    // if permission granted after prompt
                    if (result) {
                        showImagePicker();
                    } else {
                        Snackbar.make(mView,
                                "[INFO]: File Access was denied."
                                , Snackbar.LENGTH_SHORT).show();
                    }
                }
            });

    public final ActivityResultLauncher<Intent> imgUploadResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {

                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if (data == null) return;

                        Uri selectedImageUri = data.getData();
                        if (selectedImageUri != null){
                            try {
                                Glide.with((Context) mActivity)
                                        .load(selectedImageUri)
                                        .into(tutorAvatarImageView);
                            }catch (Exception e){
                                SnackBarHelper.showSnackBar(mView, e.getMessage());
                            }
                        }
                    }
                }
            });

    private void promptFilePermission() {
        // for android 11 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri));

            showImagePicker();
        } else {
            checkFilePermissionForOldAPI();
        }

    }

    private void showImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imgUploadResultLauncher.launch(intent);
    }

    private void navigateToBasicInfo() {
        Intent intent = new Intent(getActivity(), BasicInfoActivity.class);
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