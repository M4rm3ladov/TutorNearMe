package com.ren.tutornearme.data;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.ren.tutornearme.controller.VolleySingleton;
import com.ren.tutornearme.model.Barangay;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class BarangayBank {
    ArrayList<Barangay> barangayArrayList = new ArrayList<>();
    private String url = "https://psgc.gitlab.io/api/cities/097332000/barangays.json";

    public List<Barangay> getBarangays(final BarangayListAsyncResponse callBack) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for(int i = 0; i < response.length(); i++) {
                            try {
                                Barangay barangay = new Barangay();
                                barangay.setName(response.getJSONObject(i).getString("name"));

                                barangayArrayList.add(barangay);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if(callBack != null) callBack.processFinished(barangayArrayList);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        VolleySingleton.getInstance().addToRequestQueue(jsonArrayRequest);
        return barangayArrayList;
    }
}
