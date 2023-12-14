package com.ren.tutornearme.ui.subject;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.ren.tutornearme.R;
import com.ren.tutornearme.adapter.SubjectAdapter;
import com.ren.tutornearme.databinding.FragmentSubjectBinding;
import com.ren.tutornearme.model.TutorSubject;
import com.ren.tutornearme.util.InputValidatorHelper;

import java.util.ArrayList;

public class SubjectFragment extends Fragment implements View.OnClickListener {

    private SubjectViewModel subjectViewModel;
    private FragmentSubjectBinding binding;
    private Context mContext;
    private View mView;
    private Application mApplication;
    private ArrayList<TutorSubject> tutorSubjectArrayList;
    private SubjectAdapter subjectRequestAdapter;

    private AutoCompleteTextView searchAutoTextView;
    private RecyclerView subjectRequestRecyclerView;
    private FloatingActionButton addSubjectFab;

    private InputValidatorHelper inputValidatorHelper = new InputValidatorHelper();
    private Button sendButton;
    private ImageView backImageView;
    private EditText subjectNameEditText, subjectDescriptionEditText;
    private TextInputLayout subjectNameInputLayout, subjectDescriptionInputLayout;
    private ProgressBar progressBar;

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        if (getActivity() != null)
            mApplication = getActivity().getApplication();
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mContext = null;
        mApplication = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        subjectViewModel = new ViewModelProvider(this).get(SubjectViewModel.class);
        mView = container;

        binding = FragmentSubjectBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        initBindFragmentViews();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initBindFragmentViews() {
        subjectRequestRecyclerView = binding.subjectRecyclerView;
        searchAutoTextView = binding.subjectSearchEditText;
        addSubjectFab = binding.subjectFloatingActionButton;
        addSubjectFab.setOnClickListener(this);

        /*configureRecyclerView();
        attachSearchInputListener();*/
    }

    private void configureRecyclerView() {
        subjectRequestRecyclerView.setHasFixedSize(true);
        subjectRequestRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        tutorSubjectArrayList = new ArrayList<>();
        subjectRequestAdapter = new SubjectAdapter(
                subjectViewModel, getViewLifecycleOwner(), mContext);
        subjectRequestRecyclerView.setAdapter(subjectRequestAdapter);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.subject_floatingActionButton) {
            navigateToSubjectRequest();
        }
    }

    private void navigateToSubjectRequest() {
        startActivity(new Intent(getActivity(), SubjectRequestActivity.class));
    }

    private void initAttachClickListeners() {
        sendButton.setOnClickListener(this);
        backImageView.setOnClickListener(this);
        subjectNameEditText.setOnClickListener(this);
    }

    private void initAttachInputListeners() {
        subjectNameEditText.setOnFocusChangeListener((view, isFocused) -> {
            String subjectName = subjectNameEditText.getText().toString().trim();
            if (!isFocused) {
                if (inputValidatorHelper.isNullOrEmpty(subjectName)) {
                    subjectNameInputLayout.setHelperText("Please fill in subject name.");
                }
            }
        });

        subjectNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                subjectNameInputLayout.setHelperText("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        subjectDescriptionEditText.setOnFocusChangeListener((view, isFocused) -> {
            String subjectDescription = subjectDescriptionEditText.getText().toString().trim();
            if (!isFocused) {
                if (inputValidatorHelper.isNullOrEmpty(subjectDescription)) {
                    subjectDescriptionInputLayout.setHelperText("Please fill in subject description.");
                }
            }
        });

        subjectDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                subjectDescriptionInputLayout.setHelperText("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}