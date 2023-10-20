package com.ren.tutornearme.util;

import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class SnackBarHelper {
    public static void showSnackBar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        TextView snackTextView = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);

        snackTextView.setMaxLines(5);
        snackbar.show();
    }
}
