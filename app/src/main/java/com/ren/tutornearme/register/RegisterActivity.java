package com.ren.tutornearme.register;

import static com.ren.tutornearme.util.Common.BARANGAY;
import static com.ren.tutornearme.util.Common.CURRENT_USER;
import static com.ren.tutornearme.util.Common.FIRST_NAME;
import static com.ren.tutornearme.util.Common.GENDER;
import static com.ren.tutornearme.util.Common.LAST_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.ren.tutornearme.HomeActivity;
import com.ren.tutornearme.R;
import com.ren.tutornearme.auth.MainActivity;
import com.ren.tutornearme.data.AddressBank;
import com.ren.tutornearme.data.DataOrException;
import com.ren.tutornearme.model.TutorInfo;
import com.ren.tutornearme.util.InputValidatorHelper;
import com.ren.tutornearme.util.InternetHelper;

import org.parceler.Parcels;

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
    private RegisterViewModel profileViewModel;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initBindViews();
        initCheckSender();
        initSetButtonListeners();
        initAttachInputListeners();
        initBarangayList();
        initAuthViewModel();
    }

    private void initCheckSender() {
        bundle = getIntent().getExtras();
        if (bundle != null) {
            firstNameEditText.setText(bundle.getString(FIRST_NAME));
            lastNameEditText.setText(bundle.getString(LAST_NAME));
            barangayEditText.setText(bundle.getString(BARANGAY));

            Resources res = getResources();
            String male = res.getString(R.string.male);
            String female = res.getString(R.string.female);
            String privateGender = res.getString(R.string.i_d_rather_not_say);

            if ((male).equals(bundle.getString(GENDER))) genderRadioGroup.check(R.id.male_radio_button);
            else if ((female).equals(bundle.getString(GENDER))) genderRadioGroup.check(R.id.female_radio_button);
            else if ((privateGender).equals(bundle.getString(GENDER))) genderRadioGroup.check(R.id.private_gender_radio_button);

            TextView registerMessage = findViewById(R.id.register_message_textview);
            registerMessage.setText(R.string.update_basic_info_message);
            registerButton.setText(R.string.save);

        }
    }

    @Override
    protected void onResume() {
        if (!InternetHelper.isOnline(getApplication())) {
            Snackbar.make(findViewById(android.R.id.content),
                    "[ERROR]: No internet connection. Please check your network",
                    Snackbar.LENGTH_SHORT).show();
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (bundle != null)
            super.onBackPressed();
        else
            navigateToSignIn();

    }

    private void navigateToSignIn() {
        profileViewModel.signOut();
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void initSetButtonListeners() {
        maleRadioButton.setOnClickListener(this);
        femaleRadioButton.setOnClickListener(this);
        privateGenderRadioButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (bundle != null)
                finish();
            else
                navigateToSignIn();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initBindViews() {
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
    }

    private void initBarangayList() {
        barangayList = new AddressBank().getBarangays(new AddressBank.AddressListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<String> barangayArrayList) {
                // Add barangay list to adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, barangayArrayList);
                AutoCompleteTextView barangayTextView = findViewById(R.id.barangay_auto_textview);
                barangayTextView.setAdapter(adapter);
            }
        });
    }
    private void initAuthViewModel() {
        profileViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication()))
                .get(RegisterViewModel.class);
    }

    private void initAttachInputListeners() {
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
            if (!InternetHelper.isOnline(getApplication())) {
                Snackbar.make(findViewById(android.R.id.content),
                        "[ERROR]: No internet connection. Please check your network",
                        Snackbar.LENGTH_SHORT).show();
                return;
            }
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
        tutorInfo.setResume("");
        tutorInfo.setValidId("");
        tutorInfo.setCreatedDate(epoch);
        tutorInfo.setUpdatedDate(epoch);

        progressBar.setVisibility(View.VISIBLE);
        profileViewModel.registerTutor(tutorInfo).observe(this,
                new Observer<DataOrException<TutorInfo, Exception>>() {
                    @Override
                    public void onChanged(DataOrException<TutorInfo, Exception> dataOrException) {
                        progressBar.setVisibility(View.GONE);
                        if (dataOrException.exception != null) {
                            Snackbar.make(findViewById(android.R.id.content),
                                    "[ERROR]: " + dataOrException.exception.getMessage(),
                                    Snackbar.LENGTH_SHORT).show();
                            return;
                        }

                        if (dataOrException.data != null) {
                            Toast.makeText(RegisterActivity.this, "Saved successfully",
                                    Toast.LENGTH_SHORT).show();

                            Intent intent;
                            if (bundle == null) {
                                intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                bundle.putParcelable(CURRENT_USER, Parcels.wrap(tutorInfo));
                                intent.putExtras(bundle);
                            } else {
                                intent = new Intent(RegisterActivity.this, MainActivity.class);
                            }
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    private void validateBeforeSave() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String barangay = barangayEditText.getText().toString().trim();
        if (!barangay.isEmpty())
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