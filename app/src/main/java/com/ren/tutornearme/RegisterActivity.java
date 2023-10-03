package com.ren.tutornearme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ren.tutornearme.data.AddressBank;
import com.ren.tutornearme.data.AddressListAsyncResponse;
import com.ren.tutornearme.model.TutorInfo;
import com.ren.tutornearme.util.Common;
import com.ren.tutornearme.util.InputValidatorHelper;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText firstNameEditText, lastNameEditText;
    private AutoCompleteTextView barangayEditText;
    private RadioGroup genderRadioGroup;
    private RadioButton maleRadioButton, femaleRadioButton, checkedGenderRadioButton;
    private TextInputLayout firstNameInputLayout, lastNameInputLayout, genderInputLayout, barangayInputLayout;
    private Button registerButton;
    private boolean isValid = true;
    private List<String> barangayList;
    private InputValidatorHelper inputValidatorHelper = new InputValidatorHelper();

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(Common.TUTOR_INFO_REFERENCE);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        firstNameEditText = findViewById(R.id.first_name_edit_text);
        lastNameEditText = findViewById(R.id.last_name_edit_text);
        genderRadioGroup = findViewById(R.id.gender_radio_group);
        barangayEditText = findViewById(R.id.barangay_auto_textview);
        maleRadioButton = findViewById(R.id.male_radio_button);
        femaleRadioButton = findViewById(R.id.female_radio_button);
        firstNameInputLayout = findViewById(R.id.first_name_input_layout);
        lastNameInputLayout = findViewById(R.id.last_name_input_layout);
        genderInputLayout = findViewById(R.id.gender_input_layout);
        barangayInputLayout = findViewById(R.id.barangay_input_layout);
        registerButton = findViewById(R.id.register_button);

        maleRadioButton.setOnClickListener(this);
        femaleRadioButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);

        // Populate barangay auto complete edit text
        barangayList = new AddressBank().getBarangays(new AddressListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<String> barangayArrayList) {
                // Add barangay list to adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, barangayArrayList);
                AutoCompleteTextView barangayTextView = (AutoCompleteTextView)
                        findViewById(R.id.barangay_auto_textview);
                barangayTextView.setAdapter(adapter);
            }
        });
        // Input validations
        attachInputListeners();
    }

    private void attachInputListeners() {
        firstNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                String firstName = firstNameEditText.getText().toString().trim();
                if (!isFocused) {
                    if (inputValidatorHelper.isNullOrEmpty(firstName)) {
                        firstNameInputLayout.setHelperText("Please fill in first name.");
                        isValid = false;
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
                String firstName = lastNameEditText.getText().toString().trim();
                if (!isFocused) {
                    if (inputValidatorHelper.isNullOrEmpty(firstName)) {
                        lastNameInputLayout.setHelperText("Please fill in last name.");
                        isValid = false;
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
                if (!isFocused) {
                    if (inputValidatorHelper.isNullOrEmpty(barangayName)) {
                        barangayInputLayout.setHelperText("Please fill in Barangay.");
                        isValid = false;
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
        if (view.getId() == R.id.male_radio_button || view.getId() == R.id.female_radio_button) {
            genderInputLayout.setHelperText("");
        }
    }

    private void saveTutorInfo() {
        validateBeforeSave();

        currentUser = firebaseAuth.getCurrentUser();
        // check if passed validation and has user

        if (!isValid) return;

        String currentUserUid = currentUser.getUid();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String gender = checkedGenderRadioButton.getText().toString();
        String phoneNumber = currentUser.getPhoneNumber();
        String barangay = barangayEditText.getText().toString().trim();

        TutorInfo tutorInfo = new TutorInfo();
        tutorInfo.setUid(currentUserUid);
        tutorInfo.setFirstName(firstName);
        tutorInfo.setLastName(lastName);
        tutorInfo.setGender(gender);
        tutorInfo.setPhoneNumber(phoneNumber);
        tutorInfo.setAddress(barangay);

        if (currentUser != null) {
            collectionReference.add(tutorInfo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(RegisterActivity.this, "Successfully saved information", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "[ERROR]: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        }
    }

    private void validateBeforeSave() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String barangay = barangayEditText.getText().toString().trim();

        if (!barangayList.contains(barangay)) {
            barangayInputLayout.setHelperText("Please fill in valid Barangay.");
            isValid = false;
        }
        if (!maleRadioButton.isChecked() && !femaleRadioButton.isChecked()) {
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
        if (inputValidatorHelper.isNullOrEmpty(barangay)) {
            barangayInputLayout.setHelperText("Please fill in Barangay.");
            isValid = false;
        }
    }
}