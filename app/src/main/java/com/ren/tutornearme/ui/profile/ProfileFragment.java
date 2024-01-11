package com.ren.tutornearme.ui.profile;

import static com.ren.tutornearme.util.Common.BARANGAY;
import static com.ren.tutornearme.util.Common.BIRTH_DATE;
import static com.ren.tutornearme.util.Common.EMAIL;
import static com.ren.tutornearme.util.Common.FIRST_NAME;
import static com.ren.tutornearme.util.Common.GENDER;
import static com.ren.tutornearme.util.Common.LAST_NAME;
import static com.ren.tutornearme.util.Common.RESUBMIT;
import static com.ren.tutornearme.util.Common.SUBMITTED;
import static com.ren.tutornearme.util.Common.UNVERIFIED;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;
import com.ren.tutornearme.BuildConfig;
import com.ren.tutornearme.R;
import com.ren.tutornearme.SharedViewModel;
import com.ren.tutornearme.contact_info.ContactInfoActivity;
import com.ren.tutornearme.model.TutorInfo;
import com.ren.tutornearme.basic_info.BasicInfoActivity;
import com.ren.tutornearme.util.InternetHelper;
import com.ren.tutornearme.util.SnackBarHelper;
import com.stfalcon.imageviewer.StfalconImageViewer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment implements View.OnClickListener, PickiTCallbacks {
    private CardView basicInfoCardView, emailCardView;
    private Button submitButton;
    private TextView tutorName, tutorGender, tutorBarangay, tutorBirthDate, tutorResume, tutorID, tutorEmail;
    private ImageView tutorAvatarImageView, previewIdImageView, previewResumeImageView;
    private Spinner validIdTypeSpinner;
    private AlertDialog waitingDialog;

    private Resources res;

    private ProfileViewModel profileViewModel;
    private SharedViewModel sharedViewModel;
    protected TutorInfo mTutorInfo;

    private FragmentActivity mActivity;
    private View mView;

    private PickiT pickiT;
    private enum PickiTFlag { PDF, IMG }
    private PickiTFlag pickiTFlag;
    private String imageFlag;

    private static final String AVATAR_IMG = "avatar";
    private static final String ID_IMG = "validId";

    
    private final SimpleDateFormat dateTimeFormatter =
            new SimpleDateFormat( "MMM-dd-yyyy" , Locale.ENGLISH);

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
        pickiT = new PickiT(mActivity, this, mActivity);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mView = view;

        initBindViews(view);
        initPopulateBasicInfo();
        sharedViewModel.getTutorAccountText().observe(mActivity, accountStatus -> {
            if (accountStatus.equals(RESUBMIT) || accountStatus.equals(UNVERIFIED))
                initBindListeners(this);
            else if (accountStatus.equals(SUBMITTED))
                initBindListeners(null);
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pickiT.deleteTemporaryFile(mActivity);
    }

    @Override
    public void onDetach() {
        pickiT.deleteTemporaryFile(mActivity);
        super.onDetach();
    }

    @Override
    public void onResume() {
        if (!InternetHelper.isOnline(mActivity.getApplication()))
            Snackbar.make(mView,
                    "[ERROR]: No internet connection. Please check your network",
                    Snackbar.LENGTH_SHORT).show();
        sharedViewModel.setTutorInfoListener();
        super.onResume();
    }

    @Override
    public void onPause() {
        sharedViewModel.removeTutorInfoListener();
        super.onPause();
    }

    private void initViewModels() {
        profileViewModel =
                new ViewModelProvider(mActivity).get(ProfileViewModel.class);

        sharedViewModel = new ViewModelProvider(mActivity).get(SharedViewModel.class);
    }

    private void initBindListeners(View.OnClickListener clickListener) {
        basicInfoCardView.setOnClickListener(clickListener);
        emailCardView.setOnClickListener(clickListener);
        tutorAvatarImageView.setOnClickListener(clickListener);
        tutorResume.setOnClickListener(clickListener);
        tutorID.setOnClickListener(clickListener);
        previewIdImageView.setOnClickListener(clickListener);
        previewResumeImageView.setOnClickListener(clickListener);
        submitButton.setOnClickListener(clickListener);
    }

    private void initBindViews(View view) {
        res = getResources();

        basicInfoCardView = view.findViewById(R.id.profile_basic_info_cardview);
        emailCardView = view.findViewById(R.id.profile_email_cardview);
        tutorAvatarImageView = view.findViewById(R.id.profile_img_imageview);
        validIdTypeSpinner = view.findViewById(R.id.profile_id_type_spinner);
        previewIdImageView = view.findViewById(R.id.profile_id_preview);
        previewResumeImageView = view.findViewById(R.id.profile_resume_preview);
        submitButton = view.findViewById(R.id.profile_submit_button);

        tutorName = view.findViewById(R.id.profile_tutor_name_textview);
        tutorGender = view.findViewById(R.id.profile_tutor_gender_textview);
        tutorBarangay = view.findViewById(R.id.profile_tutor_brgy_textview);
        tutorBirthDate = view.findViewById(R.id.profile_tutor_birth_textview);
        tutorResume = view.findViewById(R.id.profile_tutor_resume_textview);
        tutorID = view.findViewById(R.id.profile_tutor_id_textview);
        tutorEmail = view.findViewById(R.id.profile_tutor_email_textview);
    }

    private void initPopulateBasicInfo() {
        sharedViewModel.getTutorInfoFromFirebase().observe(mActivity, dataOrException -> {
            if (dataOrException.exception != null) {
                SnackBarHelper.showSnackBar(mView,
                        "[ERROR]: " + dataOrException.exception.getMessage());
                return;
            }

            if (dataOrException.data != null) {
                sharedViewModel.setTutorInfo(dataOrException.data);
                TutorInfo tutorInfo = dataOrException.data;

                String name = String.format(res.getString(R.string.tutor_name),
                        tutorInfo.getFirstName(), tutorInfo.getLastName());
                tutorName.setText(name);
                tutorGender.setText(tutorInfo.getGender());
                tutorBarangay.setText(tutorInfo.getAddress());
                tutorBirthDate.setText(dateTimeFormatter.format(new Date(tutorInfo.getBirthDate())));

                // avatar
                String avatarPath = "";
                if (!tutorInfo.getAvatar().isEmpty())
                    avatarPath = tutorInfo.getAvatar();
                if (profileViewModel.getAvatarPath().getValue() != null)
                    avatarPath = profileViewModel.getAvatarPath().getValue();

                if (!avatarPath.isEmpty())
                    Glide.with((Context) mActivity)
                            .load(avatarPath)
                            .placeholder(R.mipmap.ic_logo)
                            .apply(new RequestOptions().override(100, 100))
                            .into(tutorAvatarImageView);

                if (!tutorInfo.getEmail().isEmpty())
                    tutorEmail.setText(tutorInfo.getEmail());

                // valid id text field
                if (profileViewModel.getValidIdPath().getValue() != null &&
                        profileViewModel.getValidIdUri().getValue() != null) {
                    tutorID.setText(profileViewModel.getValidIdPath().getValue());

                } else if (tutorInfo.getValidId() != null && !tutorInfo.getValidId().isEmpty()) {
                    tutorID.setText(tutorInfo.getValidId());
                    profileViewModel.setValidIdPath(tutorInfo.getValidId());
                    profileViewModel.setValidIdUri(Uri.parse(tutorInfo.getValidId()));

                } else tutorID.setText(res.getString(R.string.choose_a_file_to_upload));

                // resume text field
                if (profileViewModel.getResumePath().getValue() != null &&
                        profileViewModel.getResumeUri().getValue() != null) {
                    tutorResume.setText(profileViewModel.getResumePath().getValue());

                } else if (tutorInfo.getResume() != null && !tutorInfo.getResume().isEmpty()) {
                    tutorResume.setText(tutorInfo.getResume());
                    profileViewModel.setResumePath(tutorInfo.getResume());
                    profileViewModel.setResumeUri(Uri.parse(tutorInfo.getResume()));

                } else tutorResume.setText(res.getString(R.string.choose_a_file_to_upload));

            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.profile_basic_info_cardview) {
            navigateToBasicInfo();

        } else if (view.getId() == R.id.profile_email_cardview) {
            navigateToContactInfo();

        } else if (view.getId() == R.id.profile_img_imageview) {
            imageFlag = AVATAR_IMG;
            promptImageFilePermission();

        } else if (view.getId() == R.id.profile_tutor_id_textview) {
            imageFlag = ID_IMG;
            promptImageFilePermission();

        } else if (view.getId() == R.id.profile_tutor_resume_textview) {
            promptResumeMessage();

        } else if (view.getId() == R.id.profile_id_preview) {
            if(!checkHasInternetConnection()) return;
            showIdPreviewOrSnackbar();

        } else if (view.getId() == R.id.profile_resume_preview) {
            if(!checkHasInternetConnection()) return;
            showResumePreview();

        } else if(view.getId() == R.id.profile_submit_button) {
            if(!checkHasInternetConnection()) return;
            submitApplication();

        }
    }

    private void submitApplication() {
        if (sharedViewModel.getTutorInfo().getEmail().isEmpty()) {
            SnackBarHelper.showSnackBar(mView, "Please provide your email.");
            return;
        }

        if (sharedViewModel.getTutorInfo().getAvatar() == null || sharedViewModel.getTutorInfo().getAvatar().isEmpty()) {
            SnackBarHelper.showSnackBar(mView, "Please upload a clear image of your face before submitting.");
            return;
        }

        if (profileViewModel.getValidIdUri().getValue() == null) {
            SnackBarHelper.showSnackBar(mView, "Please upload your valid ID before submitting");
            return;
        }

        if (profileViewModel.getResumeUri().getValue() == null) {
            SnackBarHelper.showSnackBar(mView, "Please upload your resume before submitting");
            return;
        }

        profileViewModel.setTutorAccountStatus().observe(this, dataOrException -> {
            if (dataOrException.exception != null) {
                SnackBarHelper.showSnackBar(mView,
                        "[ERROR]: " + dataOrException.exception.getMessage());
            }

            if (dataOrException.data != null) {
                Toast.makeText(mActivity,
                        "Successfully submitted! Please wait for verification.", Toast.LENGTH_SHORT).show();
                mActivity.onBackPressed();
            }
        });
    }

    private void showResumePreview() {
        if (profileViewModel.getResumePath().getValue() == null)
            Snackbar.make(mView, "Please choose a file to preview.",
                    Snackbar.LENGTH_SHORT).show();
        else {
            Uri pdfUri = profileViewModel.getResumeUri().getValue();
            Intent intent = new Intent(Intent.ACTION_VIEW, pdfUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }

    private void showIdPreviewOrSnackbar() {
        if (profileViewModel.getValidIdUri().getValue() == null)
            Snackbar.make(mView, "Please choose a file to preview.",
                    Snackbar.LENGTH_SHORT).show();
        else {
            Uri currentIdUri = profileViewModel.getValidIdUri().getValue();
            new StfalconImageViewer.Builder<>
                    (mActivity, Collections.singletonList(currentIdUri), (imageView, image) ->
                        Glide.with((Context) mActivity)
                                .load(image)
                                .placeholder(R.mipmap.ic_logo)
                                .into(imageView)

                    )
                    .withHiddenStatusBar(false)
                    .allowZooming(true)
                    .allowSwipeToDismiss(true)
                    .show();
        }
    }

    private boolean checkHasInternetConnection() {
        if (!InternetHelper.isOnline(mActivity.getApplication())) {
            Snackbar.make(mView,
                    "[ERROR]: No internet connection. Please check your network",
                    Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void checkFilePermissionForOldAPI() {
        if (ActivityCompat.checkSelfPermission(mActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            // requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            showImagePicker();
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
                                "File Access was denied."
                                , Snackbar.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> imgUploadResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {

                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if (data == null) return;

                        Uri uri = data.getData();
                        if (imageFlag.equals(AVATAR_IMG))
                            uploadAvatarImage(uri);

                        else if (imageFlag.equals(ID_IMG)) {
                            pickiTFlag = PickiTFlag.IMG;
                            pickiT.getPath(uri, Build.VERSION.SDK_INT);

                            profileViewModel.setValidIdUri(uri);

                            if(!checkHasInternetConnection()) return;
                            uploadValidId();
                        }
                    }
                }
            });
    private final ActivityResultLauncher<Intent> pdfUploadResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {

                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if (data == null) return;

                        Uri selectedResumeUri = data.getData();

                        pickiTFlag = PickiTFlag.PDF;
                        pickiT.getPath(selectedResumeUri, Build.VERSION.SDK_INT);

                        profileViewModel.setResumeUri(selectedResumeUri);

                        if(!checkHasInternetConnection()) return;
                        uploadResume();
                    }
                }
            });
    private void createWaitingDialog() {
        waitingDialog = new AlertDialog.Builder(mActivity)
                .setCancelable(false)
                .setMessage("Uploading..")
                .create();
    }

    private void uploadValidId() {
        if (profileViewModel.getValidIdUri().getValue() == null) {
            Snackbar.make(mView, "Please choose a file before uploading.",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        createWaitingDialog();
        
        Uri selectedImageUri = profileViewModel.getValidIdUri().getValue();
        if (selectedImageUri != null) {
            waitingDialog.show();
            try {
                profileViewModel.uploadAvatarOrValidId(selectedImageUri, imageFlag).observe(this,
                        mapDataOrException -> {

                            if (mapDataOrException.exception != null) {
                                SnackBarHelper.showSnackBar(mView, mapDataOrException.exception.getMessage());
                                waitingDialog.dismiss();
                                return;
                            }

                            if (mapDataOrException.data != null) {
                                if (mapDataOrException.data.get("progress") != null) {
                                    double progress = Math.round((double) mapDataOrException.data.get("progress"));
                                    waitingDialog.setMessage(new StringBuilder("Uploading: ")
                                            .append(progress)
                                            .append("%"));
                                }

                                if (mapDataOrException.data.get("isComplete") != null) {
                                    waitingDialog.dismiss();
                                    updateTutorValidId();
                                }
                            }
                        });

            }catch (Exception e){
                SnackBarHelper.showSnackBar(mView, e.getMessage());
            }
        }
    }

    private void uploadResume() {
        if (profileViewModel.getResumeUri().getValue() == null) {
            Snackbar.make(mView, "Please choose a file before uploading.",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        createWaitingDialog();

        Uri selectedPdfUri = profileViewModel.getResumeUri().getValue();
        if (selectedPdfUri != null) {
            waitingDialog.show();

            try {
                profileViewModel.uploadResume(selectedPdfUri).observe(this,
                        mapDataOrException -> {
                            if (mapDataOrException.exception != null) {
                                SnackBarHelper.showSnackBar(mView, mapDataOrException.exception.getMessage());
                                waitingDialog.dismiss();
                                return;
                            }

                            if (mapDataOrException.data != null) {
                                if (mapDataOrException.data.get("progress") != null) {
                                    double progress = Math.round((double) mapDataOrException.data.get("progress"));
                                    waitingDialog.setMessage(new StringBuilder("Uploading: ")
                                            .append(progress)
                                            .append("%"));
                                }

                                if (mapDataOrException.data.get("isComplete") != null) {
                                    waitingDialog.dismiss();
                                    updateTutorResume();
                                }
                            }
                        });
            } catch (Exception e){
                SnackBarHelper.showSnackBar(mView, e.getMessage());
            }
        }
    }

    private void uploadAvatarImage(Uri selectedImageUri) {

        createWaitingDialog();

        if (selectedImageUri != null) {
            waitingDialog.show();
            try {
                profileViewModel.uploadAvatarOrValidId(selectedImageUri, imageFlag).observe(this,
                        mapDataOrException -> {
                                if (mapDataOrException.exception != null) {
                                    SnackBarHelper.showSnackBar(mView, mapDataOrException.exception.getMessage());
                                    waitingDialog.dismiss();
                                    return;
                                }

                                if (mapDataOrException.data != null) {
                                    if (mapDataOrException.data.get("progress") != null) {
                                        double progress = Math.round((double) mapDataOrException.data.get("progress"));
                                        waitingDialog.setMessage(new StringBuilder("Uploading: ")
                                                        .append(progress)
                                                        .append("%"));
                                    }

                                    if (mapDataOrException.data.get("isComplete") != null) {
                                        updateTutorAvatar(selectedImageUri, waitingDialog);
                                    }
                                }
                        });

            }catch (Exception e){
                SnackBarHelper.showSnackBar(mView, e.getMessage());
            }
        }
    }

    private void updateTutorValidId() {
        String selectedIdType = validIdTypeSpinner.getSelectedItem().toString();
        profileViewModel.updateAvatarOrValidId(ID_IMG, selectedIdType).observe(this,
                dataOrException -> {
                    if (dataOrException.exception != null) {
                        SnackBarHelper.showSnackBar(mView, dataOrException.exception.getMessage());
                        return;
                    }

                    if (dataOrException.data != null) {
                        if (dataOrException.data) {
                            Toast.makeText(mActivity, "Valid ID uploaded.", Toast.LENGTH_SHORT)
                                    .show();
                        }

                    }
                });
    }

    private void updateTutorResume() {
        profileViewModel.updateResume().observe(this,
                dataOrException -> {
                    if (dataOrException.exception != null) {
                        SnackBarHelper.showSnackBar(mView, dataOrException.exception.getMessage());
                        return;
                    }

                    if (dataOrException.data != null) {
                        if (dataOrException.data) {
                            Toast.makeText(mActivity, "Resume uploaded.", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    private void updateTutorAvatar(Uri selectedImageUri, AlertDialog waitingDialog) {
        profileViewModel.updateAvatarOrValidId(AVATAR_IMG, null).observe(this,
                dataOrException -> {
                    if (dataOrException.exception != null) {
                        SnackBarHelper.showSnackBar(mView, dataOrException.exception.getMessage());
                        return;
                    }

                    if (dataOrException.data != null) {
                        if (dataOrException.data) {
                            Toast.makeText(mActivity, "Image saved successfully!", Toast.LENGTH_SHORT)
                                    .show();
                            Glide.with((Context) mActivity)
                                    .load(selectedImageUri)
                                    .placeholder(R.mipmap.ic_logo_round)
                                    .apply(new RequestOptions().override(100, 100))
                                    .into(tutorAvatarImageView);
                            waitingDialog.dismiss();

                            profileViewModel.setAvatarPath(selectedImageUri.toString());
                        }

                    }
                });
    }

    private void promptResumeMessage() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Upload PDF")
                .setMessage(res.getString(R.string.tell_us_something_about_your_education_experience_and_skills))
                .setPositiveButton("Ok",
                        (dialogInterface, i)-> showPDFPicker())
                .show();
    }

    private void promptImageFilePermission() {
        String message = "";
        if (AVATAR_IMG.equals(imageFlag))
            message = "Please make sure to upload a clear image of your face without " +
                    "wearing any accessories on.";
        else if (ID_IMG.equals(imageFlag))
            message = "Please upload a clear and original copy of you valid ID.";

        new AlertDialog.Builder(getActivity())
                .setTitle("Upload Image")
                .setMessage(message)
                .setPositiveButton("Ok",
                (dialogInterface, i)-> {
                    // for android 11 and above
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                        Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                        startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri));

                        showImagePicker();
                    } else {
                        checkFilePermissionForOldAPI();
                    }
                })
                .show();
    }

    private void showImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imgUploadResultLauncher.launch(intent);
    }

    private void showPDFPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pdfUploadResultLauncher.launch(intent);
    }

    private void navigateToBasicInfo() {
        Intent intent = new Intent(getActivity(), BasicInfoActivity.class);
        Bundle bundle = new Bundle();

        mTutorInfo = sharedViewModel.getTutorInfo();

        bundle.putString(FIRST_NAME, mTutorInfo.getFirstName());
        bundle.putString(LAST_NAME, mTutorInfo.getLastName());
        bundle.putString(GENDER, mTutorInfo.getGender());
        bundle.putString(BARANGAY, mTutorInfo.getAddress());
        bundle.putLong(BIRTH_DATE, mTutorInfo.getBirthDate());

        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void navigateToContactInfo() {
        Intent intent = new Intent(getActivity(), ContactInfoActivity.class);

        intent.putExtra(EMAIL, sharedViewModel.getTutorInfo().getEmail());
        startActivity(intent);
    }

    @Override
    public void PickiTonUriReturned() {

    }

    @Override
    public void PickiTonStartListener() {

    }

    @Override
    public void PickiTonProgressUpdate(int progress) {

    }

    @Override
    public void PickiTonCompleteListener(String path, boolean wasDriveFile, boolean wasUnknownProvider, boolean wasSuccessful, String Reason) {
        if (wasSuccessful) {
            if (pickiTFlag == PickiTFlag.IMG) {
                profileViewModel.setValidIdPath(path);
                tutorID.setText(path);
            } else if (pickiTFlag == PickiTFlag.PDF) {
                profileViewModel.setResumePath(path);
                tutorResume.setText(path);
            }
        } else
            SnackBarHelper.showSnackBar(mView, Reason);

    }

    @Override
    public void PickiTonMultipleCompleteListener(ArrayList<String> paths, boolean wasSuccessful, String Reason) {

    }
}