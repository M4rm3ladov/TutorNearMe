package com.ren.tutornearme;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.ren.tutornearme.data.BarangayBank;
import com.ren.tutornearme.data.BarangayListAsyncResponse;
import com.ren.tutornearme.model.Barangay;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private List<Barangay> barangayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        barangayList = new BarangayBank().getBarangays(new BarangayListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<Barangay> barangayArrayList) {
                List<String> barangayNames = new ArrayList<>();
                for(Barangay b: barangayArrayList) {
                    barangayNames.add(b.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, barangayNames);
                AutoCompleteTextView barangayTextView = (AutoCompleteTextView)
                        findViewById(R.id.barangay_auto_textview);
                barangayTextView.setAdapter(adapter);
            }
        });
        Log.d("BARANGAY", "onCreate: " + barangayList);
    }
}