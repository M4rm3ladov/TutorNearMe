package com.ren.tutornearme.ui.subject;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.view.MenuItem;

import com.ren.tutornearme.R;

public class SubjectRequestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_request);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back is pressed... Finishing the activity
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.subject_host_fragment);
        if (navController.getCurrentDestination() == null) finish();
        int id = navController.getCurrentDestination().getId();

        if (item.getItemId() == android.R.id.home) {
            if (id == R.id.subjectFilesFragment)
                Navigation.findNavController(findViewById(R.id.subject_host_fragment))
                        .navigate(R.id.subjectListFragment);
            else
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}