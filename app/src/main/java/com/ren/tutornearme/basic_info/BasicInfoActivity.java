package com.ren.tutornearme.basic_info;

import static com.ren.tutornearme.util.Common.BARANGAY;
import static com.ren.tutornearme.util.Common.BIRTH_DATE;
import static com.ren.tutornearme.util.Common.CURRENT_USER;
import static com.ren.tutornearme.util.Common.FIRST_NAME;
import static com.ren.tutornearme.util.Common.GENDER;
import static com.ren.tutornearme.util.Common.LAST_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
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
import com.ren.tutornearme.util.SnackBarHelper;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BasicInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText firstNameEditText, lastNameEditText, birthDateEditText;
    private AutoCompleteTextView barangayEditText;
    private RadioGroup genderRadioGroup;
    private RadioButton maleRadioButton, femaleRadioButton, privateGenderRadioButton;
    private TextInputLayout firstNameInputLayout, lastNameInputLayout, genderInputLayout,
            barangayInputLayout, birthDateInputLayout;
    private Button saveButton;
    private ProgressBar progressBar;

    private Bundle bundle;
    private Resources res;

    private boolean isValid = true;
    private final InputValidatorHelper inputValidatorHelper = new InputValidatorHelper();

    private BasicInfoViewModel basicInfoViewModel;
    private List<String> barangayList;

    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateTimeFormatter =
            new SimpleDateFormat( "dd-MMM-yyyy" , Locale.ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_info);

        initBindViews();
        initCheckSender();
        initSetClickListeners();
        initAttachInputListeners();
        initBarangayList();
        initAuthViewModel();
    }

    private void initCheckSender() {
        if (bundle != null) {
            firstNameEditText.setText(bundle.getString(FIRST_NAME));
            lastNameEditText.setText(bundle.getString(LAST_NAME));
            barangayEditText.setText(bundle.getString(BARANGAY));

            birthDateEditText.setText(dateTimeFormatter.format(new Date(bundle.getLong(BIRTH_DATE))));

            calendar.setTimeInMillis(bundle.getLong(BIRTH_DATE));

            String male = res.getString(R.string.male);
            String female = res.getString(R.string.female);
            String privateGender = res.getString(R.string.i_d_rather_not_say);

            if ((male).equals(bundle.getString(GENDER))) genderRadioGroup.check(R.id.male_radio_button);
            else if ((female).equals(bundle.getString(GENDER))) genderRadioGroup.check(R.id.female_radio_button);
            else if ((privateGender).equals(bundle.getString(GENDER))) genderRadioGroup.check(R.id.private_gender_radio_button);

            TextView registerMessage = findViewById(R.id.register_message_textview);
            registerMessage.setText(R.string.update_basic_info_message);
            saveButton.setText(R.string.save);

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
        basicInfoViewModel.signOut();
        Intent intent = new Intent(BasicInfoActivity.this, MainActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void initSetClickListeners() {
        maleRadioButton.setOnClickListener(this);
        femaleRadioButton.setOnClickListener(this);
        privateGenderRadioButton.setOnClickListener(this);
        birthDateEditText.setOnClickListener(this);
        saveButton.setOnClickListener(this);
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
        birthDateEditText = findViewById(R.id.birth_date_edit_text);
        birthDateEditText.setFocusable(false);
        birthDateEditText.setClickable(true);
        birthDateEditText.setLongClickable(false);

        genderRadioGroup = findViewById(R.id.gender_radio_group);
        maleRadioButton = findViewById(R.id.male_radio_button);
        femaleRadioButton = findViewById(R.id.female_radio_button);
        privateGenderRadioButton = findViewById(R.id.private_gender_radio_button);

        firstNameInputLayout = findViewById(R.id.first_name_input_layout);
        lastNameInputLayout = findViewById(R.id.last_name_input_layout);
        genderInputLayout = findViewById(R.id.gender_input_layout);
        barangayInputLayout = findViewById(R.id.barangay_input_layout);
        birthDateInputLayout = findViewById(R.id.birth_date_input_layout);

        saveButton = findViewById(R.id.basic_info_save_button);

        bundle = getIntent().getExtras();
        res = getResources();
    }

    private void initBarangayList() {
        barangayList = new AddressBank().getBarangays(new AddressBank.AddressListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<String> barangayArrayList) {
                // Add barangay list to adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(BasicInfoActivity.this,
                        android.R.layout.simple_dropdown_item_1line, barangayArrayList);
                AutoCompleteTextView barangayTextView = findViewById(R.id.barangay_auto_textview);
                barangayTextView.setAdapter(adapter);
            }
        });
    }
    private void initAuthViewModel() {
        basicInfoViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication()))
                .get(BasicInfoViewModel.class);
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
        birthDateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                birthDateInputLayout.setHelperText("");
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String birthDate = birthDateEditText.getText().toString();
                if (!inputValidatorHelper.isNullOrEmpty(birthDate)) {
                    try {
                        calendar.setTime(dateTimeFormatter.parse(birthDate));
                    } catch (ParseException e) {
                        SnackBarHelper.showSnackBar(findViewById(android.R.id.content), e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.basic_info_save_button) {
            if (!InternetHelper.isOnline(getApplication())) {
                Snackbar.make(findViewById(android.R.id.content),
                        "[ERROR]: No internet connection. Please check your network",
                        Snackbar.LENGTH_SHORT).show();
                return;
            }
            saveTutorInfo();

        } else if (view.getId() == R.id.birth_date_edit_text) {
            showDatePickerDialog();
        }

        if (view.getId() == R.id.male_radio_button || view.getId() == R.id.female_radio_button
                || view.getId() == R.id.private_gender_radio_button) {
            genderInputLayout.setHelperText("");
        }
    }

    private void showDatePickerDialog() {
        //setDateIfEditTextHasValue();

        DatePickerDialog datePickerDialog = new DatePickerDialog(BasicInfoActivity.this,
                R.style.MaterialCalendarTheme,
                new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,day);
                birthDateEditText.setText(dateTimeFormatter.format(calendar.getTime()));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    /*private void setDateIfEditTextHasValue() {

    }*/

    private void saveTutorInfo() {
        validateBeforeSave();

        // check if passed validation and has user
        FirebaseUser currentUser = basicInfoViewModel.getCurrentUser();
        if (!isValid) return;
        if (currentUser == null) return;

        int selectedId = genderRadioGroup.getCheckedRadioButtonId();
        RadioButton checkedGenderRadioButton = findViewById(selectedId);

        String currentUserUid = currentUser.getUid();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String gender = checkedGenderRadioButton.getText().toString();
        String phoneNumber = currentUser.getPhoneNumber();
        String barangay = barangayEditText.getText().toString().trim();
        long currentDate = System.currentTimeMillis();
        long birthDate = calendar.getTimeInMillis();
        boolean isVerified = false;

        progressBar.setVisibility(View.VISIBLE);

        TutorInfo tutorInfo = new TutorInfo();
        tutorInfo.setFirstName(firstName);
        tutorInfo.setLastName(lastName);
        tutorInfo.setGender(gender);
        tutorInfo.setAddress(barangay);
        tutorInfo.setBirthDate(birthDate);
        tutorInfo.setUpdatedDate(currentDate);

        // existing account
        if (bundle != null) {
            Map<String, Object> postValues =  tutorInfo.toMap();
            basicInfoViewModel.updateTutorInfo(postValues).observe(this, new Observer<DataOrException<TutorInfo, Exception>>() {
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
                        Toast.makeText(BasicInfoActivity.this, "Updated successfully",
                                Toast.LENGTH_SHORT).show();

                        navigateToHome(dataOrException.data, bundle);
                    }
                }
            });
            return;
        }

        // creating new user account
        tutorInfo.setUid(currentUserUid);
        tutorInfo.setPhoneNumber(phoneNumber);
        tutorInfo.setResume("");
        tutorInfo.setValidId("");
        tutorInfo.setValidIdType("");
        tutorInfo.setAvatar("");
        tutorInfo.setVerified(isVerified);
        tutorInfo.setCreatedDate(currentDate);

        basicInfoViewModel.saveTutorInfo(tutorInfo).observe(this,
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
                            Toast.makeText(BasicInfoActivity.this, "Saved successfully",
                                    Toast.LENGTH_SHORT).show();

                            navigateToHome(dataOrException.data, new Bundle());
                        }
                    }
                });
    }

    private void navigateToHome(TutorInfo tutorInfo, Bundle bundle) {
        Intent intent = new Intent(BasicInfoActivity.this, HomeActivity.class);
        bundle.putParcelable(CURRENT_USER, Parcels.wrap(tutorInfo));
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void validateBeforeSave() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String barangay = barangayEditText.getText().toString().trim();
        String birthDate = birthDateEditText.getText().toString();
        if (!barangay.isEmpty())
            barangay = barangay.substring(0, 1).toUpperCase() + barangay.substring(1);

        isValid = true;

        if (inputValidatorHelper.isNullOrEmpty(birthDate)) {
            birthDateInputLayout.setHelperText("Please fill in birth date.");
            isValid = false;
        } else {
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            if (inputValidatorHelper.isNotLegalAge(year, month + 1, dayOfMonth)){
                birthDateInputLayout.setHelperText("Only ages 18 and up are allowed to our services.");
                isValid = false;
            }
        }

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