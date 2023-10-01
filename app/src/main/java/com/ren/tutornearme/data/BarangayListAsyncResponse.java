package com.ren.tutornearme.data;

import com.ren.tutornearme.model.Barangay;

import java.util.ArrayList;

public interface BarangayListAsyncResponse {
    void processFinished(ArrayList<Barangay> barangayArrayList);
}
