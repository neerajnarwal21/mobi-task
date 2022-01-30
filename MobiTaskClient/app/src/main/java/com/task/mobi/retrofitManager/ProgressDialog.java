package com.task.mobi.retrofitManager;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.task.mobi.R;

/**
 * Created by neeraj on 27/7/17.
 */
public class ProgressDialog {

    private static Context context;
    private static ProgressDialog instace;
    private Dialog progressDialog;
    private TextView txtMsgTV;

    public static ProgressDialog getInstance(Context context) {
        ProgressDialog.context = context;
        if (instace == null) {
            instace = new ProgressDialog();
        }
        return instace;
    }

    public void initiateProgressDialog() {
        progressDialog = new Dialog(context);
        View view = View.inflate(context, R.layout.progress_dialog, null);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(view);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        txtMsgTV = (TextView) view.findViewById(R.id.txtMsgTV);
        progressDialog.setCancelable(false);
    }

    public void startProgressDialog() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            try {
                progressDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
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
