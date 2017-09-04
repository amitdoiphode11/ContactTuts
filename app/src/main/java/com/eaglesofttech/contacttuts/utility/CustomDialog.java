package com.eaglesofttech.contacttuts.utility;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by ADMIN1 on 23-Dec-16.
 */
public class CustomDialog {
    Activity activity;
    ProgressDialog progressDialog;

    public CustomDialog(Activity activity) {
        this.activity = activity;
    }

    public void showProgressDialog(String message) {
        //progressDialog = null;
        hideProgressDialog();
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage(message);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.hide();
            progressDialog = null;
        }
    }



    public void hideKeyBoard() {

        InputMethodManager inputManager = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

    }

}
