package com.ren.tutornearme.ui.subject.subject_file;

import static com.ren.tutornearme.util.Common.CURRENT_SUBJECT;
import static com.ren.tutornearme.util.Common.UNVERIFIED;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;
import com.ren.tutornearme.R;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.databinding.FragmentSubjectFilesBinding;
import com.ren.tutornearme.model.SubjectInfo;
import com.ren.tutornearme.model.TutorInfo;
import com.ren.tutornearme.model.TutorSubject;
import com.ren.tutornearme.ui.subject.SubjectSharedViewModel;
import com.ren.tutornearme.util.InternetHelper;
import com.ren.tutornearme.util.SnackBarHelper;

import org.parceler.Parcels;

import java.util.ArrayList;

public class SubjectFilesFragment extends Fragment implements View.OnClickListener, PickiTCallbacks {
    private SubjectSharedViewModel subjectSharedViewModel;
    private SubjectFilesViewModel subjectFilesViewModel;
    private FragmentSubjectFilesBinding binding;
    private View mView;
    private Application mApplication;
    private FragmentActivity mActivity;
    private Resources res;
    private NavController navController;
    private SubjectInfo subjectInfo;
    private TutorInfo tutorInfo;

    private TextView subjectFileTextView;
    private ImageView subjectFilePreviewImageView;
    private Button submitRequestButton;
    private AlertDialog waitingDialog;

    private Uri selectedResumeUri;
    private PickiT pickiT;
    private boolean hasUploadedFile = false;

    @Override
    public void onAttach(@NonNull Context context) {
        if (getActivity() != null) {
            mApplication = getActivity().getApplication();
            mActivity = getActivity();
        }
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mApplication = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mActivity.getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navController.navigate(R.id.action_subjectFilesFragment_to_subjectListFragment);
            }
        });

        pickiT = new PickiT(mActivity, this, mActivity);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        subjectSharedViewModel =
                new ViewModelProvider(this).get(SubjectSharedViewModel.class);
        subjectFilesViewModel =
                new ViewModelProvider(this).get(SubjectFilesViewModel.class);

        binding = FragmentSubjectFilesBinding.inflate(inflater, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null)
            subjectInfo = Parcels.unwrap(bundle.getParcelable(CURRENT_SUBJECT));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        mView = view;
        res = getResources();
        initBindFragmentViews();
        super.onViewCreated(view, savedInstanceState);
    }

    private void initBindFragmentViews() {
        subjectFileTextView = binding.subjectFileTextview;
        subjectFilePreviewImageView = binding.subjectFilePreview;
        submitRequestButton = binding.subjectFileButton;

        subjectFileTextView.setOnClickListener(this);
        subjectFilePreviewImageView.setOnClickListener(this);
        submitRequestButton.setOnClickListener(this);
    }

    private boolean checkHasInternetConnection() {
        if (!InternetHelper.isOnline(mApplication)) {
            Snackbar.make(mView,
                    "[ERROR]: No internet connection. Please check your network",
                    Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.subject_file_preview) {
            showSubjectFilePreview();
        } if (view.getId() == R.id.subject_file_textview) {
            promptFileUploadMessage();
        } if (view.getId() == R.id.subject_file_button) {
            getTutorInfo();
        }
    }

    private void getTutorInfo() {
        if (!hasUploadedFile) {
            SnackBarHelper.showSnackBar(mView, "Please upload a file before submitting request.");
            return;
        }

        subjectSharedViewModel.getTutorInfo().observe(this, dataOrException -> {
            if (dataOrException.exception != null) {
                Snackbar.make(mView,
                        "[ERROR]: " + dataOrException.exception.getMessage(),
                        Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (dataOrException.data != null) {
                tutorInfo = dataOrException.data;
                uploadCredential();
            }
        });

    }

    private void sendTutorSubjectRequest(String credentialUriPath) {
        long currentDate = System.currentTimeMillis();
        TutorSubject tutorSubject = new TutorSubject();
        tutorSubject.setTutorInfo(tutorInfo);
        tutorSubject.setSubjectInfo(subjectInfo);
        tutorSubject.setStatus(UNVERIFIED);
        tutorSubject.setCredential(credentialUriPath);
        tutorSubject.setCreatedDate(currentDate);
        tutorSubject.setUpdatedDate(currentDate);

        subjectSharedViewModel.saveTutorSubject(tutorSubject).observe(this, dataOrException -> {
            if (dataOrException.exception != null) {
                Snackbar.make(mView,
                        "[ERROR]: " + dataOrException.exception.getMessage(),
                        Snackbar.LENGTH_SHORT).show();
                waitingDialog.dismiss();
                return;
            }

            if (dataOrException.data != null) {
                saveTutorSubjectLookUp();
            }
        });
    }

    private void uploadCredential() {
        createWaitingDialog();
        waitingDialog.show();
        subjectFilesViewModel.uploadCredential(selectedResumeUri).observe(this,  mapDataOrException -> {
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
                    String credentialUriPath = (String) mapDataOrException.data.get("isComplete");
                    sendTutorSubjectRequest(credentialUriPath);
                    //saveTutorSubjectLookUp(key);
                }
            }
        });
    }

    private void saveTutorSubjectLookUp() {
        subjectSharedViewModel.saveTutorSubjectLookUp(subjectInfo.getId())
                .observe(this, new Observer<DataOrException<Boolean, Exception>>() {
                    @Override
                    public void onChanged(DataOrException<Boolean, Exception> dataOrException) {
                        if (dataOrException.exception != null) {
                            SnackBarHelper.showSnackBar(mView, dataOrException.exception.getMessage());
                            waitingDialog.dismiss();
                            return;
                        }

                        if (dataOrException.data != null) {
                            if (dataOrException.data) {
                                waitingDialog.dismiss();
                                Toast.makeText(mActivity, "Request has been submitted.", Toast.LENGTH_SHORT)
                                        .show();
                                mActivity.finish();
                            }
                        }
                    }
                });
    }

    private void showSubjectFilePreview() {
        if(!checkHasInternetConnection()) return;

        if (!hasUploadedFile)
            Snackbar.make(mView, "Please choose a file to preview.",
                    Snackbar.LENGTH_SHORT).show();
        else {
            Uri pdfUri = selectedResumeUri;
            Intent intent = new Intent(Intent.ACTION_VIEW, pdfUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }

    private void promptFileUploadMessage() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Upload PDF")
                .setMessage(res.getString(R.string.subject_credential_message))
                .setPositiveButton("Ok",
                        (dialogInterface, i)-> showPDFPicker())
                .show();
    }

    private void showPDFPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pdfUploadResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> pdfUploadResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {

                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if (data == null) return;

                        selectedResumeUri = data.getData();
                        pickiT.getPath(selectedResumeUri, Build.VERSION.SDK_INT);
                    }
                }
            });

    private void createWaitingDialog() {
        waitingDialog = new AlertDialog.Builder(mActivity)
                .setCancelable(false)
                .setMessage("Uploading..")
                .create();
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
            subjectFileTextView.setText(path);
            hasUploadedFile = true;
        } else
            SnackBarHelper.showSnackBar(mView, Reason);
    }

    @Override
    public void PickiTonMultipleCompleteListener(ArrayList<String> paths, boolean wasSuccessful, String Reason) {

    }
}