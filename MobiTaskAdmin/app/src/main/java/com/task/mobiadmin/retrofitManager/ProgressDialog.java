package com.task.mobiadmin.retrofitManager;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.task.mobiadmin.R;

/**
 * Created by neeraj on 27/7/17.
 */

public class ProgressDialog {

    private Context context;
    private Dialog progressDialog;
    private TextView txtMsgTV;

    public ProgressDialog(Context context) {
        this.context = context;
        initiateProgressDialog();
    }

    private void initiateProgressDialog() {
        progressDialog = new Dialog(context);
        View view = View.inflate(context, R.layout.progress_dialog, null);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(view);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        txtMsgTV = view.findViewById(R.id.txtMsgTV);
        progressDialog.setCancelable(false);
    }

    public void startProgressDialog() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            try {
                progressDialog.show();
            } catch (Exception ignored) {
            }
        }
    }

    public void stopProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateMessage(String message) {
        txtMsgTV.setText(message);
    }
}
