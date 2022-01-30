package com.task.mobiadmin.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.task.mobiadmin.R;
import com.task.mobiadmin.activity.BaseActivity;
import com.task.mobiadmin.activity.MainActivity;
import com.task.mobiadmin.fragment.home.HomeFragment;
import com.task.mobiadmin.retrofitManager.ApiClient;
import com.task.mobiadmin.retrofitManager.ApiInterface;
import com.task.mobiadmin.retrofitManager.ApiManager;
import com.task.mobiadmin.retrofitManager.ResponseListener;
import com.task.mobiadmin.utils.PrefStore;

import retrofit2.Call;


/**
 * Created by Neeraj Narwal on 2/5/17.
 */
public class BaseFragment extends Fragment implements AdapterView.OnItemClickListener,
        View.OnClickListener, AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener, ResponseListener {

    public BaseActivity baseActivity;
    public ApiInterface apiInterface;
    public PrefStore store;
    public ApiManager apiManager;
    ApiClient apiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseActivity = (BaseActivity) getActivity();
        baseActivity.hideSoftKeyboard();
        apiClient = baseActivity.apiClient;
        apiManager = baseActivity.apiManager;
        store = baseActivity.store;
        apiInterface = baseActivity.apiInterface;
        try {
            baseActivity.setSupportActionBar(((MainActivity) baseActivity).toolBar);
        } catch (Exception ignored) {
        }
    }

    public void setToolbar(boolean show, String title) {
        ((MainActivity) baseActivity).setToolbar(show, title);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onClick(View v) {

    }

    public void showToast(String msg) {
        baseActivity.showToast(msg, false);
    }

    public void showToast(String msg, boolean isError) {
        baseActivity.showToast(msg, isError);
    }

    public void log(String s) {
        baseActivity.log(s);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    public String getText(TextView textView) {
        return textView.getText().toString().trim();
    }

    public void gotoHomeFragment() {
        baseActivity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, new HomeFragment())
                .commit();
    }

    @Override
    public void onSuccess(Call call, Object object) {

    }

    @Override
    public void onError(Call call, int statusCode, String errorMessage, ResponseListener responseListener) {
        baseActivity.onError(call, statusCode, errorMessage, responseListener);
    }
}
