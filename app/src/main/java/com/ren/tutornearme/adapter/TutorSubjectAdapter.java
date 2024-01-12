package com.ren.tutornearme.adapter;

import android.app.Application;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.ren.tutornearme.R;
import com.ren.tutornearme.model.TutorSubject;
import com.ren.tutornearme.ui.subject.tutor_subject.TutorSubjectViewModel;
import com.ren.tutornearme.util.InputValidatorHelper;
import com.ren.tutornearme.util.InternetHelper;
import com.ren.tutornearme.util.SnackBarHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TutorSubjectAdapter extends RecyclerView.Adapter<TutorSubjectAdapter.SubjectViewHolder>{

    private final TutorSubjectViewModel tutorSubjectViewModel;
    private final LifecycleOwner lifecycleOwner;
    private final Context context;
    private View view;

    private AlertDialog alertDialog;
    private Button saveButton;
    private ImageView backImageView;
    private EditText tutorHourEditText;
    private TextInputLayout tutorHourInputLayout;
    private boolean isValid = true;
    private final InputValidatorHelper inputValidatorHelper = new InputValidatorHelper();

    private ArrayList<TutorSubject> tutorSubjectArrayList = new ArrayList<>();
    private final SimpleDateFormat dateTimeFormatter =
            new SimpleDateFormat( "dd-MMM-yyyy, hh:mm" , Locale.ENGLISH);

    public TutorSubjectAdapter(Context context, TutorSubjectViewModel tutorSubjectViewModel, LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.tutorSubjectViewModel = tutorSubjectViewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(context).inflate(R.layout.tutor_subject_row, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        TutorSubject tutorSubject = tutorSubjectArrayList.get(position);
        holder.subjectNameTextView.setText(tutorSubject.getSubjectInfo().getName());
        holder.subjectDescriptionTextView.setText(tutorSubject.getSubjectInfo().getDescription());
        holder.subjectHourLength.setText(tutorSubject.getSessionHours() > 1 ?
                String.format("%s hrs", tutorSubject.getSessionHours()) :
                String.format("%s hr", tutorSubject.getSessionHours()));
        holder.updatedDateTextView
                .setText(dateTimeFormatter.format(new Date(tutorSubject.getSubjectInfo().getUpdatedDate())));
        holder.logoImageView.setBackgroundResource(R.mipmap.ic_logo_round);
        holder.subjectSwitch.setChecked(tutorSubject.isAvailable());

        holder.rowCardView.setOnClickListener(view -> {
            createUpdateDialogBuilder(position);
        });

        holder.subjectSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            saveSubjectAvailability(position, isChecked);
        });
    }

    private void saveSubjectAvailability(int position, boolean isChecked) {

        String key = tutorSubjectArrayList.get(position).getId();

        tutorSubjectViewModel.setTutorSubjectAvailability(key, isChecked)
                .observe(lifecycleOwner, dataOrException -> {
                    if (dataOrException.exception != null) {
                        SnackBarHelper.showSnackBar(view, dataOrException.exception.getMessage());
                        return;
                    }

                    if (dataOrException.data != null) {
                        Toast.makeText(context, "Subject enabled.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createUpdateDialogBuilder(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View tutorHourDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_tutor_hour, null);
        initBindDialogViews(tutorHourDialogView);
        initAttachButtonListeners(position);
        initAttachInputListeners();
        tutorHourEditText.setText(String.valueOf(tutorSubjectArrayList.get(position).getSessionHours()));

        builder.setView(tutorHourDialogView);
        builder.setCancelable(false);
        alertDialog = builder.create();
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        alertDialog.show();

    }

    private void initBindDialogViews(View view) {
        tutorHourEditText = view.findViewById(R.id.tutor_hour_editText);
        saveButton = view.findViewById(R.id.tutor_hour_saveButton);
        backImageView = view.findViewById(R.id.tutor_hour_imageView);
        tutorHourInputLayout = view.findViewById(R.id.tutor_hour_inputLayout);
    }

    private void initAttachButtonListeners(int position) {
        saveButton.setOnClickListener(view -> {
            if (!InternetHelper.isOnline((Application) context.getApplicationContext())) {
                Snackbar.make(view,
                        "[ERROR]: No internet connection. Please check your network",
                        Snackbar.LENGTH_SHORT).show();
                return;
            }
            saveTutorSubjectSession(position);
        });
        backImageView.setOnClickListener(view -> {
            alertDialog.dismiss();
        });
    }

    private void saveTutorSubjectSession(int position) {
        validateBeforeSave();
        if (!isValid) return;

        int sessionHours = Integer.parseInt(tutorHourEditText.getText().toString().trim());
        String key = tutorSubjectArrayList.get(position).getId();

        tutorSubjectViewModel.setTutorSubjectSession(key, sessionHours)
                .observe(lifecycleOwner, dataOrException -> {
                    if (dataOrException.exception != null) {
                        SnackBarHelper.showSnackBar(view, dataOrException.exception.getMessage());
                        return;
                    }

                    if (dataOrException.data != null) {
                        Toast.makeText(context, "Saved successfully",
                                Toast.LENGTH_SHORT).show();
                    }
                    alertDialog.dismiss();
                });
    }

    private void validateBeforeSave() {
        String tutorHour = tutorHourEditText.getText().toString().trim();
        isValid = true;

        if (inputValidatorHelper.isNullOrEmpty(tutorHour)) {
            tutorHourInputLayout.setHelperText("Fill in session.");
            isValid = false;
        } else if (!inputValidatorHelper.isNumeric(tutorHour)) {
            tutorHourInputLayout.setHelperText("Fill in a valid hour.");
            isValid = false;
        } else if (Integer.parseInt(tutorHour) <= 0) {
            tutorHourInputLayout.setHelperText("Minimum of 1.");
            isValid = false;
        }

    }

    private void initAttachInputListeners() {
        tutorHourEditText.setOnFocusChangeListener((view, isFocused) -> {

            String tutorHour = tutorHourEditText.getText().toString().trim();
            if (!isFocused) {
                if (inputValidatorHelper.isNullOrEmpty(tutorHour))
                    tutorHourInputLayout.setHelperText("Fill in session.");
                else if (!inputValidatorHelper.isNumeric(tutorHour))
                    tutorHourInputLayout.setHelperText("Fill in a valid hour.");
                else if (Integer.parseInt(tutorHour) <= 0)
                    tutorHourInputLayout.setHelperText("Minimum of 1.");
            }
        });

        tutorHourEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tutorHourInputLayout.setHelperText("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return tutorSubjectArrayList.size();
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {

        private final TextView subjectNameTextView, subjectDescriptionTextView, subjectHourLength, updatedDateTextView;
        private final ImageView logoImageView;
        private final CardView rowCardView;
        private final MaterialSwitch subjectSwitch;
        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);

            subjectNameTextView = itemView.findViewById(R.id.tutor_subject_name_textView);
            subjectDescriptionTextView = itemView.findViewById(R.id.tutor_subject_description_textView);
            subjectHourLength = itemView.findViewById(R.id.tutor_subject_session_hour_textView);
            updatedDateTextView = itemView.findViewById(R.id.tutor_subject_updated_date);
            logoImageView = itemView.findViewById(R.id.tutor_subject_logo_imageView);
            rowCardView = itemView.findViewById(R.id.tutor_subject_row_cardView);
            subjectSwitch = itemView.findViewById(R.id.tutor_subject_switch);
        }
    }

    public void setTutorSubjectList(ArrayList<TutorSubject> filteredList) {
        tutorSubjectArrayList = filteredList;
        notifyDataSetChanged();
    }
}
