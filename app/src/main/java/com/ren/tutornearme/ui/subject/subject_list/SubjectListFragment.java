package com.ren.tutornearme.ui.subject.subject_list;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.material.snackbar.Snackbar;
import com.ren.tutornearme.adapter.SubjectListAdapter;
import com.ren.tutornearme.databinding.FragmentSubjectListBinding;
import com.ren.tutornearme.model.SubjectInfo;
import com.ren.tutornearme.util.InternetHelper;
import com.ren.tutornearme.util.SnackBarHelper;

import java.util.ArrayList;

public class SubjectListFragment extends Fragment implements View.OnClickListener {
    private FragmentSubjectListBinding binding;
    private Context mContext;
    private View mView;
    private Application mApplication;
    private NavController navController;
    private SubjectListViewModel subjectListViewModel;
    private SubjectListAdapter subjectListAdapter;
    private ArrayList<SubjectInfo> subjectInfoArrayList;

    private AutoCompleteTextView searchAutoTextView;
    private RecyclerView subjectListRecyclerView;

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
    public void onResume() {
        if (!InternetHelper.isOnline(mApplication))
            Snackbar.make(mView,
                    "[ERROR]: No internet connection. Please check your network",
                    Snackbar.LENGTH_SHORT).show();

        subjectListViewModel.getSubjectInfoList().observe(getViewLifecycleOwner(), dataOrException -> {
            if (dataOrException.exception != null) {
                SnackBarHelper.showSnackBar(mView,  "[ERROR]: " + dataOrException.exception.getMessage());
                return;
            }

            if (dataOrException.data != null) {
                subjectInfoArrayList.clear();
                subjectInfoArrayList.addAll(dataOrException.data);
                subjectInfoArrayList.sort((s1, s2) -> Long.compare(s2.getUpdatedDate(), s1.getUpdatedDate()));
                subjectListAdapter.setSubjectList(subjectInfoArrayList);

                ArrayAdapter<SubjectInfo> adapter = new ArrayAdapter<>(mContext,
                        android.R.layout.simple_dropdown_item_1line, subjectInfoArrayList);
                searchAutoTextView.setAdapter(adapter);
            }
        });
        super.onResume();
    }

    @Override
    public void onPause() {
        subjectListViewModel.removeSubjectInfoListener();
        super.onPause();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        mView = view;
        initBindFragmentViews();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        subjectListViewModel =
                new ViewModelProvider(this).get(SubjectListViewModel.class);

        binding = FragmentSubjectListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        /*initBindFragmentViews();*/

        return root;
    }

    private void initBindFragmentViews() {
        subjectListRecyclerView = binding.subjectListRecyclerView;
        searchAutoTextView = binding.subjectListSearchEditText;

        configureRecyclerView();
        attachSearchInputListener();
    }

    private void configureRecyclerView() {
        subjectListRecyclerView.setHasFixedSize(true);
        subjectListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        subjectInfoArrayList = new ArrayList<>();
        subjectListAdapter = new SubjectListAdapter(
                subjectListViewModel, getViewLifecycleOwner(), mContext, navController);
        subjectListRecyclerView.setAdapter(subjectListAdapter);
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
                ArrayList<SubjectInfo> filteredList = new ArrayList<>();

                for (SubjectInfo subjectItem: subjectInfoArrayList) {
                    if (subjectItem.getName().toLowerCase().contains(text.toLowerCase()) ||
                            subjectItem.getDescription().toLowerCase().contains(text.toLowerCase())) {
                        filteredList.add(subjectItem);
                    }
                }
                subjectListAdapter.setSubjectList(filteredList);
            }
        });
    }

    @Override
    public void onClick(View view) {

    }
}