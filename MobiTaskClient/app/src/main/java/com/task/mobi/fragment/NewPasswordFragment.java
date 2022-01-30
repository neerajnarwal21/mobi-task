package com.task.mobi.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.gson.JsonObject;
import com.task.mobi.R;
import com.task.mobi.utils.Const;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
public class NewPasswordFragment extends BaseFragment {

    public View view;

    @BindView(R.id.oldPassET)
    EditText oldPassET;

    @BindView(R.id.newPassET)
    EditText newPassET;

    @BindView(R.id.confirmET)
    EditText confirmET;

    private Call<JsonObject> updateCall;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fg_new_password, null);
        }
        setToolbar(true, "Change Password");
        return view;
    }

    @OnClick(R.id.submitBT)
    public void submit() {
        if (validate()) {
            updatePassword();
        }
    }

    private void updatePassword() {
        RequestBody sessId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID));
        RequestBody oldPass = RequestBody.create(MediaType.parse("text/plain"), getText(oldPassET));
        RequestBody newPass = RequestBody.create(MediaType.parse("text/plain"), getText(newPassET));

        updateCall = apiInterface.changePassword(sessId, oldPass, newPass);
        apiManager.makeApiCall(updateCall, this);
    }

    private boolean validate() {
        if (getText(oldPassET).isEmpty()) {
            showToast("Please enter Old Password", true);
        } else if (getText(newPassET).isEmpty()) {
            showToast("Please enter New Password", true);
        } else if (getText(confirmET).isEmpty()) {
            showToast("Please enter Confirm Password", true);
        } else if (!getText(newPassET).equals(getText(confirmET))) {
            showToast("Passwords doesn't match");
        } else
            return true;
        return false;
    }

    @Override
    public void onSuccess(Call call, Object object) {
        super.onSuccess(call, object);
        if (updateCall == call) {
            showToast("Password Updated Successfully");
            baseActivity.onBackPressed();
            baseActivity.onBackPressed();
        }
    }
}
