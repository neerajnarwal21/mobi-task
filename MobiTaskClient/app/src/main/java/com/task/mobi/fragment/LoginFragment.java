package com.task.mobi.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.task.mobi.BuildConfig;
import com.task.mobi.R;
import com.task.mobi.activity.MainActivity;
import com.task.mobi.data.UserData;
import com.task.mobi.utils.Const;
import com.task.mobi.utils.MacUtils;

import java.lang.reflect.Type;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
public class LoginFragment extends BaseFragment {

    public View view;

    @BindView(R.id.emailET)
    EditText emailET;

    @BindView(R.id.passwordET)
    EditText passwordET;

    @BindView(R.id.rememberCB)
    CheckBox rememberCB;

    private Call<JsonObject> loginCall;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fg_login, null);
        }
//        Utils.setStatusBarTranslucent(baseActivity, true);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (BuildConfig.DEBUG) {
            emailET.setText("guru.p@pugmarks.com");
            passwordET.setText("admin123");
        }
        if (store.getBoolean(Const.REMEMBER_ME)) {
            rememberCB.setChecked(true);
            emailET.setText(store.getString(Const.REMEMBER_EMAIL));
            passwordET.setText(store.getString(Const.REMEMBER_PASS));
        }
    }

    @OnClick(R.id.loginBT)
    public void submit() {
        if (validate()) {
            login();
        }
    }

    @OnClick(R.id.forgotTV)
    public void resetPassword() {
        baseActivity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container_lf, new ForgotPasswordFragment())
                .addToBackStack(null)
                .commit();
    }

    private void login() {
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String mac = MacUtils.macAddress(wifiManager);

        RequestBody email = RequestBody.create(MediaType.parse("text/plain"), getText(emailET));
        RequestBody password = RequestBody.create(MediaType.parse("text/plain"), getText(passwordET));
        RequestBody deviceId = RequestBody.create(MediaType.parse("text/plain"), mac + ":U");
        loginCall = apiInterface.login(email, password, deviceId);
        apiManager.makeApiCall(loginCall, this);
    }

    private boolean validate() {
        if (getText(emailET).isEmpty()) {
            showToast("Please enter email", true);
        } else if (getText(passwordET).isEmpty()) {
            showToast("Please enter password", true);
        } else
            return true;
        return false;
    }

    @Override
    public void onSuccess(Call call, Object object) {
        super.onSuccess(call, object);
        if (loginCall == call) {
            if (rememberCB.isChecked()) {
                store.setBoolean(Const.REMEMBER_ME, true);
                store.saveString(Const.REMEMBER_EMAIL, getText(emailET));
                store.saveString(Const.REMEMBER_PASS, getText(passwordET));
            }
            JsonObject jsonObject = (JsonObject) object;
            JsonObject data = jsonObject.getAsJsonObject("data");
            Type objectType = new TypeToken<UserData>() {
            }.getType();
            UserData userData = new Gson().fromJson(data, objectType);
            store.saveString(Const.SESSION_ID, userData.sessionId);
            store.saveString(Const.USER_ID, userData.id);
            store.saveUserData(Const.USER_DATA, userData);
            gotoMainAct();
        }
    }

    private void gotoMainAct() {
        startActivity(new Intent(baseActivity, MainActivity.class));
        baseActivity.finish();
    }
}
