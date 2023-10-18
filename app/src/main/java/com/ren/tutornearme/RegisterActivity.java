package com.ren.tutornearme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.ren.tutornearme.data.AddressBank;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.TutorInfo;
import com.ren.tutornearme.profile.ProfileViewModel;
import com.ren.tutornearme.util.InputValidatorHelper;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText firstNameEditText, lastNameEditText;
    private AutoCompleteTextView barangayEditText;
    private RadioGroup genderRadioGroup;
    private RadioButton maleRadioButton, femaleRadioButton, privateGenderRadioButton, checkedGenderRadioButton;
    private TextInputLayout firstNameInputLayout, lastNameInputLayout, genderInputLayout, barangayInputLayout;
    private Button registerButton;
    private ProgressBar progressBar;
    private boolean isValid = true;
    private List<String> barangayList;
    private final InputValidatorHelper inputValidatorHelper = new InputValidatorHelper();

    /*private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(Common.TUTOR_INFO_REFERENCE);*/
    private ProfileViewModel profileViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //firebaseAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.register_progress_bar);

        firstNameEditText = findViewById(R.id.first_name_edit_text);
        lastNameEditText = findViewById(R.id.last_name_edit_text);
        barangayEditText = findViewById(R.id.barangay_auto_textview);

        genderRadioGroup = findViewById(R.id.gender_radio_group);
        maleRadioButton = findViewById(R.id.male_radio_button);
        femaleRadioButton = findViewById(R.id.female_radio_button);
        privateGenderRadioButton = findViewById(R.id.private_gender_radio_button);

        firstNameInputLayout = findViewById(R.id.first_name_input_layout);
        lastNameInputLayout = findViewById(R.id.last_name_input_layout);
        genderInputLayout = findViewById(R.id.gender_input_layout);
        barangayInputLayout = findViewById(R.id.barangay_input_layout);

        registerButton = findViewById(R.id.register_button);

        maleRadioButton.setOnClickListener(this);
        femaleRadioButton.setOnClickListener(this);
        privateGenderRadioButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);

        // Populate barangay auto complete edit text
        initBarangayList();
        initAuthViewModel();

        // Input validations
        attachInputListeners();
    }

    private void initBarangayList() {
        barangayList = new AddressBank().getBarangays(new AddressBank.AddressListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<String> barangayArrayList) {
                // Add barangay list to adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, barangayArrayList);
                AutoCompleteTextView barangayTextView = (AutoCompleteTextView)
                        findViewById(R.id.barangay_auto_textview);
                barangayTextView.setAdapter(adapter);
            }
        });
    }
    private void initAuthViewModel() {
        profileViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication()))
                .get(ProfileViewModel.class);
    }

    private void attachInputListeners() {
        firstNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                String firstName = firstNameEditText.getText().toString().trim();
                if (!isFocused) {
                    if (inputValidatorHelper.isNullOrEmpty(firstName)) {
                        firstNameInputLayout.setHelperText("Please fill in first name.");
                    }
                }
            }
        });
        firstNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                firstNameInputLayout.setHelperText("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        lastNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                String lastName = lastNameEditText.getText().toString().trim();
                if (!isFocused) {
                    if (inputValidatorHelper.isNullOrEmpty(lastName)) {
                        lastNameInputLayout.setHelperText("Please fill in last name");
                    }
                }
            }
        });
        lastNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                lastNameInputLayout.setHelperText("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        barangayEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                String barangayName = barangayEditText.getText().toString().trim();
                if (!isFocused && !barangayName.isEmpty()) {
                    barangayName = barangayName.substring(0, 1).toUpperCase() +
                            barangayName.substring(1);
                    if (!barangayList.contains(barangayName)) {
                        barangayInputLayout.setHelperText("Please fill in valid Barangay.");
                    }
                }
            }
        });
        barangayEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                barangayInputLayout.setHelperText("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        genderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                checkedGenderRadioButton = findViewById(id);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.register_button) {
            saveTutorInfo();
        }
        if (view.getId() == R.id.male_radio_button || view.getId() == R.id.female_radio_button
                || view.getId() == R.id.private_gender_radio_button) {
            genderInputLayout.setHelperText("");
        }
    }

    private void saveTutorInfo() {
        validateBeforeSave();

        // check if passed validation and has user
        FirebaseUser currentUser = profileViewModel.getCurrentUser();
        if (!isValid) return;
        if (currentUser == null) return;

        String currentUserUid = currentUser.getUid();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String gender = checkedGenderRadioButton.getText().toString();
        String phoneNumber = currentUser.getPhoneNumber();
        String barangay = barangayEditText.getText().toString().trim();
        long epoch = System.currentTimeMillis();

        TutorInfo tutorInfo = new TutorInfo();
        tutorInfo.setUid(currentUserUid);
        tutorInfo.setFirstName(firstName);
        tutorInfo.setLastName(lastName);
        tutorInfo.setGender(gender);
        tutorInfo.setPhoneNumber(phoneNumber);
        tutorInfo.setAddress(barangay);
        tutorInfo.setCreatedDate(epoch);

        progressBar.setVisibility(View.VISIBLE);
        profileViewModel.registerTutor(tutorInfo).observe(this,
                new Observer<DataOrException<Boolean, Exception>>() {
            @Override
            public void onChanged(DataOrException<Boolean, Exception> dataOrException) {
                progressBar.setVisibility(View.GONE);
                if (dataOrException.data != null) {
                    if (dataOrException.data) {
                        Toast.makeText(RegisterActivity.this, "Saved successfully",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

                if (dataOrException.exception != null) {
                    Snackbar.make(findViewById(android.R.id.content),
                            "[ERROR]: " + dataOrException.exception.getMessage(),
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });


        /*if (currentUser != null) {
            progressBar.setVisibility(View.VISIBLE);
            collectionReference.add(tutorInfo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        documentReference.get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterActivity.this,
                                            "Successfully saved information", Toast.LENGTH_SHORT).show();

                                    Common.currentTutor = documentSnapshot.toObject(TutorInfo.class);
                                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(findViewById(android.R.id.content),
                                            "[ERROR]: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(findViewById(android.R.id.content), "[ERROR]: " + e.getMessage(),
                                Snackbar.LENGTH_LONG).show();
                    }
                });
        }*/
    }

    private void validateBeforeSave() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String barangay = barangayEditText.getText().toString().trim();
        barangay = barangay.substring(0, 1).toUpperCase() + barangay.substring(1);

        isValid = true;

        if (!barangayList.contains(barangay)) {
            barangayInputLayout.setHelperText("Please fill in valid Barangay.");
            isValid = false;
        }

        if (!maleRadioButton.isChecked() && !femaleRadioButton.isChecked() &&
                !privateGenderRadioButton.isChecked()) {
            genderInputLayout.setHelperText("Please select a gender");
            isValid = false;
        }

        if (inputValidatorHelper.isNullOrEmpty(firstName)) {
            firstNameInputLayout.setHelperText("Please fill in first name.");
            isValid = false;
        }

        if (inputValidatorHelper.isNullOrEmpty(lastName)) {
            lastNameInputLayout.setHelperText("Please fill in last name.");
            isValid = false;
        }

    }
}