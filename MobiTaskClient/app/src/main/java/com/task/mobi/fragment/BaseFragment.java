package com.task.mobi.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.task.mobi.R;
import com.task.mobi.activity.BaseActivity;
import com.task.mobi.activity.MainActivity;
import com.task.mobi.retrofitManager.ApiClient;
import com.task.mobi.retrofitManager.ApiInterface;
import com.task.mobi.retrofitManager.ApiManager;
import com.task.mobi.retrofitManager.ResponseListener;
import com.task.mobi.utils.PrefStore;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;


/**
 * Created by Neeraj Narwal on 2/5/17.
 */
public class BaseFragment extends Fragment implements AdapterView.OnItemClickListener,
        View.OnClickListener, AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener, ResponseListener {

    public BaseActivity baseActivity;
    public ApiInterface apiInterface;
    public ApiManager apiManager;
    public PrefStore store;
    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseActivity = (BaseActivity) getActivity();
        baseActivity.hideSoftKeyboard();
        apiManager = ApiManager.getInstance(baseActivity);
        apiInterface = ApiClient.getClient(baseActivity).create(ApiInterface.class);
        store = new PrefStore(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
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
