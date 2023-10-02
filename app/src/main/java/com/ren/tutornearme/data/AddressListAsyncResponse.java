package com.ren.tutornearme.data;

import com.ren.tutornearme.model.Address;

import java.util.ArrayList;

public interface AddressListAsyncResponse {
    void processFinished(ArrayList<String> barangayArrayList);
}
