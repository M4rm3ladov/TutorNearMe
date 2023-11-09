package com.ren.tutornearme.ui.profile;

import static com.ren.tutornearme.util.Common.BARANGAY;
import static com.ren.tutornearme.util.Common.BIRTH_DATE;
import static com.ren.tutornearme.util.Common.FIRST_NAME;
import static com.ren.tutornearme.util.Common.GENDER;
import static com.ren.tutornearme.util.Common.LAST_NAME;

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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;
import com.ren.tutornearme.BuildConfig;
import com.ren.tutornearme.R;
import com.ren.tutornearme.SharedViewModel;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.TutorInfo;
import com.ren.tutornearme.basic_info.BasicInfoActivity;
import com.ren.tutornearme.util.InternetHelper;
import com.ren.tutornearme.util.SnackBarHelper;
import com.stfalcon.imageviewer.StfalconImageViewer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment implements View.OnClickListener, PickiTCallbacks {
    private CardView basicInfoCardView;
    private Button uploadResumeButton, uploadValidIDButton;
    private TextView tutorName, tutorGender, tutorBarangay, tutorBirthDate, tutorResume, tutorID;
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
        initBindListeners();

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

    private void initViewModels() {
        profileViewModel =
                new ViewModelProvider(mActivity).get(ProfileViewModel.class);

        sharedViewModel = new ViewModelProvider(mActivity).get(SharedViewModel.class);
    }

    private void initBindListeners() {
        basicInfoCardView.setOnClickListener(this);
        uploadValidIDButton.setOnClickListener(this);
        uploadResumeButton.setOnClickListener(this);
        tutorAvatarImageView.setOnClickListener(this);
        tutorResume.setOnClickListener(this);
        tutorID.setOnClickListener(this);
        previewIdImageView.setOnClickListener(this);
        previewResumeImageView.setOnClickListener(this);
    }

    private void initBindViews(View view) {
        res = getResources();

        basicInfoCardView = view.findViewById(R.id.profile_basic_info_cardview);
        tutorAvatarImageView = view.findViewById(R.id.profile_img_imageview);
        uploadValidIDButton = view.findViewById(R.id.profile_upload_id_button);
        uploadResumeButton = view.findViewById(R.id.profile_upload_resume_button);
        validIdTypeSpinner = view.findViewById(R.id.profile_id_type_spinner);
        previewIdImageView = view.findViewById(R.id.profile_id_preview);
        previewResumeImageView = view.findViewById(R.id.profile_resume_preview);

        tutorName = view.findViewById(R.id.profile_tutor_name_textview);
        tutorGender = view.findViewById(R.id.profile_tutor_gender_textview);
        tutorBarangay = view.findViewById(R.id.profile_tutor_brgy_textview);
        tutorBirthDate = view.findViewById(R.id.profile_tutor_birth_textview);
        tutorResume = view.findViewById(R.id.profile_tutor_resume_textview);
        tutorID = view.findViewById(R.id.profile_tutor_id_textview);
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
                tutorBirthDate.setText(dateTimeFormatter.format(new Date(tutorInfo.getBirthDate())));

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

                if (profileViewModel.getValidIdPath().getValue() != null &&
                    profileViewModel.getValidIdUri().getValue() != null) {
                    tutorID.setText(profileViewModel.getValidIdPath().getValue());
                }

                if (profileViewModel.getResumePath().getValue() != null &&
                    profileViewModel.getResumeUri().getValue() != null) {
                    tutorResume.setText(profileViewModel.getResumePath().getValue());
                }

            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.profile_basic_info_cardview) {
            navigateToBasicInfo();
        } else if (view.getId() == R.id.profile_img_imageview) {
            imageFlag = AVATAR_IMG;
            promptImageFilePermission();
        } else if (view.getId() == R.id.profile_tutor_id_textview) {
            imageFlag = ID_IMG;
            promptImageFilePermission();
        } else if (view.getId() == R.id.profile_tutor_resume_textview) {
            showPDFPicker();
        } else if (view.getId() == R.id.profile_upload_id_button) {
            if(!checkHasInternetConnection()) return;
            uploadValidId();
        } else if (view.getId() == R.id.profile_upload_resume_button) {
            if(!checkHasInternetConnection()) return;
            uploadResume();
        } else if (view.getId() == R.id.profile_id_preview) {
            if(!checkHasInternetConnection()) return;
            showIdPreviewOrSnackbar();
        } else if (view.getId() == R.id.profile_resume_preview) {
            if(!checkHasInternetConnection()) return;
            showResumePreview();
        }
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
                    (mActivity, Arrays.asList(currentIdUri), (imageView, image) -> {
                        Glide.with((Context) mActivity)
                                .load(image)
                                .placeholder(R.mipmap.ic_logo)
                                .into(imageView);

                    })
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
                        new Observer<DataOrException<Map<String, Object>, Exception>>() {
                            @Override
                            public void onChanged(DataOrException<Map<String, Object>,
                                    Exception> mapDataOrException) {

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
                                        /*Snackbar.make(mView, "Image is now ready to be uploaded",
                                                Snackbar.LENGTH_SHORT).show();*/
                                        waitingDialog.dismiss();
                                        updateTutorValidId();
                                    }
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
                        new Observer<DataOrException<Map<String, Object>, Exception>>() {
                    @Override
                    public void onChanged(DataOrException<Map<String, Object>, Exception> mapDataOrException) {
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
                                /*Snackbar.make(mView, "File is now ready to be uploaded",
                                        Snackbar.LENGTH_SHORT).show();*/
                                waitingDialog.dismiss();
                                updateTutorResume();
                            }
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
                        new Observer<DataOrException<Map<String, Object>, Exception>>() {
                            @Override
                            public void onChanged(DataOrException<Map<String, Object>,
                                    Exception> mapDataOrException) {
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
                new Observer<DataOrException<Boolean, Exception>>() {
            @Override
            public void onChanged(DataOrException<Boolean, Exception> dataOrException) {
                if (dataOrException.exception != null) {
                    SnackBarHelper.showSnackBar(mView, dataOrException.exception.getMessage());
                    return;
                }

                if (dataOrException.data != null) {
                    if (dataOrException.data) {
                        Toast.makeText(mActivity, "Valid ID submitted for verification.", Toast.LENGTH_SHORT)
                                .show();
                        profileViewModel.setValidIdPath(null);
                        profileViewModel.setValidIdUri(null);
                        tutorID.setText(res.getString(R.string.choose_a_file_to_upload));
                    }

                }
            }
        });
    }

    private void updateTutorResume() {
        profileViewModel.updateResume().observe(this,
                new Observer<DataOrException<Boolean, Exception>>() {
            @Override
            public void onChanged(DataOrException<Boolean, Exception> dataOrException) {
                if (dataOrException.exception != null) {
                    SnackBarHelper.showSnackBar(mView, dataOrException.exception.getMessage());
                    return;
                }

                if (dataOrException.data != null) {
                    if (dataOrException.data) {
                        Toast.makeText(mActivity, "Resume submitted for verification.", Toast.LENGTH_SHORT)
                                .show();
                        profileViewModel.setResumePath(null);
                        profileViewModel.setResumeUri(null);
                        tutorResume.setText(res.getString(R.string.choose_a_file_to_upload));
                    }

                }
            }
        });
    }

    private void updateTutorAvatar(Uri selectedImageUri, AlertDialog waitingDialog) {
        profileViewModel.updateAvatarOrValidId(AVATAR_IMG, null).observe(this,
                new Observer<DataOrException<Boolean, Exception>>() {
            @Override
            public void onChanged(DataOrException<Boolean, Exception> dataOrException) {
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
            }
        });
    }

    private void promptImageFilePermission() {
        String message = "";
        if (AVATAR_IMG.equals(imageFlag))
            message = "Please make to upload image of you shows your face clearly and without " +
                    "wearing any accessories on.";
        else if (ID_IMG.equals(imageFlag))
            message = "Please submit a clear and original copy of you valid ID.";

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