package com.ren.tutornearme.ui.subject.tutor_subject;

import static com.ren.tutornearme.util.Common.VERIFIED;

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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.ren.tutornearme.R;
import com.ren.tutornearme.adapter.TutorSubjectAdapter;
import com.ren.tutornearme.databinding.FragmentSubjectBinding;
import com.ren.tutornearme.model.SubjectInfo;
import com.ren.tutornearme.model.TutorSubject;
import com.ren.tutornearme.ui.subject.SubjectRequestActivity;
import com.ren.tutornearme.util.InternetHelper;
import com.ren.tutornearme.util.SnackBarHelper;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class TutorSubjectFragment extends Fragment implements View.OnClickListener {

    private FragmentSubjectBinding binding;
    private TutorSubjectViewModel subjectViewModel;

    private Context mContext;
    private View mView;
    private Application mApplication;

    private final ArrayList<TutorSubject> tutorSubjectArrayList = new ArrayList<>();
    private ArrayList<SubjectInfo> subjectInfoArrayList;
    private TutorSubjectAdapter tutorSubjectAdapter;

    private AutoCompleteTextView searchAutoTextView;
    private RecyclerView subjectRecyclerView;
    private FloatingActionButton addSubjectFab;

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
        subjectViewModel = new ViewModelProvider(this).get(TutorSubjectViewModel.class);

        binding = FragmentSubjectBinding.inflate(inflater, container, false);

        View root = binding.getRoot();
        initBindFragmentViews();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mView = view;
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        subjectViewModel.removeTutorSubjectListener();
    }

    @Override
    public void onResume() {
        if (!InternetHelper.isOnline(mApplication))
            Snackbar.make(mView,
                    "[ERROR]: No internet connection. Please check your network",
                    Snackbar.LENGTH_SHORT).show();

        subjectViewModel.getTutorSubjectList()
                .observe(getViewLifecycleOwner(), dataOrException -> {
                    if (dataOrException.exception != null) {
                        SnackBarHelper.showSnackBar(mView,
                                "[ERROR]: " + dataOrException.exception.getMessage());
                        return;
                    }

                    if (dataOrException.data != null) {
                        tutorSubjectArrayList.clear();

                        for (TutorSubject tutorSubject : dataOrException.data)
                            if (tutorSubject.getStatus().equals(VERIFIED))
                                tutorSubjectArrayList.add(tutorSubject);

                        tutorSubjectAdapter.setTutorSubjectList(tutorSubjectArrayList);

                        subjectInfoArrayList = (ArrayList<SubjectInfo>) tutorSubjectArrayList
                                .stream().map(TutorSubject::getSubjectInfo).collect(Collectors.toList());

                        ArrayAdapter<SubjectInfo> adapter = new ArrayAdapter<>(mContext,
                                android.R.layout.simple_dropdown_item_1line, subjectInfoArrayList);
                        searchAutoTextView.setAdapter(adapter);

                        attachSearchInputListener();
                    }
                });

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initBindFragmentViews() {
        subjectRecyclerView = binding.subjectRecyclerView;
        searchAutoTextView = binding.subjectSearchEditText;
        addSubjectFab = binding.subjectFloatingActionButton;
        addSubjectFab.setOnClickListener(this);

        configureRecyclerView();
    }

    private void configureRecyclerView() {
        subjectRecyclerView.setHasFixedSize(true);
        subjectRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        tutorSubjectAdapter = new TutorSubjectAdapter(mContext,
                subjectViewModel, getViewLifecycleOwner());
        subjectRecyclerView.setAdapter(tutorSubjectAdapter);
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


    private void attachSearchInputListener() {
        searchAutoTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filterSearch(editable.toString());
            }

            private void filterSearch(String text) {
                ArrayList<TutorSubject> filteredList = new ArrayList<>();

                for (TutorSubject tutorSubject: tutorSubjectArrayList) {
                    if (tutorSubject.getSubjectInfo().getName().toLowerCase().contains(text.toLowerCase()) ||
                            tutorSubject.getSubjectInfo().getDescription().toLowerCase().contains(text.toLowerCase())) {
                        filteredList.add(tutorSubject);
                    }
                }
                tutorSubjectAdapter.setTutorSubjectList(filteredList);
            }
        });
    }
}