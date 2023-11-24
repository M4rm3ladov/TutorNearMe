package com.ren.tutornearme.contact_info;

import static com.ren.tutornearme.util.Common.EMAIL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.ren.tutornearme.R;
import com.ren.tutornearme.util.InputValidatorHelper;
import com.ren.tutornearme.util.InternetHelper;
import com.ren.tutornearme.util.SnackBarHelper;

public class ContactInfoActivity extends AppCompatActivity {
    private TextInputLayout emailInputLayout;
    private EditText emailEditText;
    private ProgressBar progressBar;
    private Button verifyEmailButton;
    private String email;

    private Bundle bundle;
    private ContactInfoViewModel contactInfoViewModel;

    private final InputValidatorHelper inputValidatorHelper = new InputValidatorHelper();

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);

        contactInfoViewModel = new ViewModelProvider(this).get(ContactInfoViewModel.class);
        bundle = getIntent().getExtras();

        initBindViews();
        initPopulateEmailIfExists();
        initAttachInputListener();
    }

    private void initPopulateEmailIfExists() {
        if (bundle != null)
            emailEditText.setText(bundle.getString(EMAIL));
    }

    private void initBindViews() {
        progressBar = findViewById(R.id.email_progress_bar);
        emailInputLayout = findViewById(R.id.email_input_layout);
        emailEditText = findViewById(R.id.email_edit_text);
        verifyEmailButton = findViewById(R.id.contact_info_verify_button);

        verifyEmailButton.setOnClickListener(view -> {
            email = emailEditText.getText().toString().trim();

            if (!inputValidatorHelper.isValidEmail(email)) {
                emailInputLayout.setHelperText("Please fill in a valid email address");
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            checkIfEmailExists();
        });
    }

    private void checkIfEmailExists() {
        contactInfoViewModel.checkIfEmailExists(email).observe(this, accountExists -> {
            progressBar.setVisibility(View.GONE);
            if (accountExists.exception != null) {
                SnackBarHelper.showSnackBar(findViewById(android.R.id.content),
                        "[ERROR]: " + accountExists.exception.getMessage());
                return;
            }

            if (accountExists.data != null) {
                if (accountExists.data)
                    SnackBarHelper.showSnackBar(findViewById(android.R.id.content),
                            "Email has already been registered to an account.");
                else
                    updateEmail();
            }
        });
    }

    private void updateEmail() {
        contactInfoViewModel.updateEmail(email).observe(this, dataOrException -> {
            if (dataOrException.exception != null) {
                SnackBarHelper.showSnackBar(findViewById(android.R.id.content),
                        "[ERROR]: " + dataOrException.exception.getMessage());
                return;
            }

            if (dataOrException.data != null) {
                if (dataOrException.data) {
                    Toast.makeText(this, "Email successfully saved",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else
                    SnackBarHelper.showSnackBar(findViewById(android.R.id.content),
                            "[ERROR]: Unable to save email right now. Try again later.");
            }
        });
    }

    private void initAttachInputListener() {
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                emailInputLayout.setHelperText("");
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}