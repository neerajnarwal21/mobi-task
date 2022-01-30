package com.task.mobi.fragment;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.task.mobi.R;
import com.task.mobi.activity.MainActivity;
import com.task.mobi.data.UserData;
import com.task.mobi.utils.AppUtils;
import com.task.mobi.utils.Const;
import com.task.mobi.utils.ImageUtils;
import com.task.mobi.utils.PermissionsManager;

import java.io.File;
import java.lang.reflect.Type;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
public class ProfileFragment extends BaseFragment implements ImageUtils.ImageSelectCallback, PermissionsManager.PermissionCallback {

    public View view;

    @BindView(R.id.nameET)
    EditText nameET;

    @BindView(R.id.phoneET)
    EditText phoneET;

    @BindView(R.id.emailET)
    EditText emailET;

    @BindView(R.id.picIV)
    ImageView picIV;

    @BindView(R.id.updateBT)
    Button updateBT;

    @BindView(R.id.changePassBT)
    Button changePassBT;

    private Call<JsonObject> updateCall;
    private UserData userData;
    private boolean isEdit;
    private File imageFile;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fg_profile, null);
        }
        setToolbar(true, "Profile");
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
    }

    private void initUI() {
        userData = (UserData) store.getUserData(Const.USER_DATA, UserData.class);
        nameET.setText(userData.name);
        phoneET.setText(userData.phone);
        emailET.setText(userData.email);
        AppUtils.MyTarget target = new AppUtils.MyTarget(picIV);
        baseActivity.picasso.load(Const.ASSETS_BASE_URL + userData.photo).error(R.mipmap.ic_default_user).placeholder(R.mipmap.ic_default_user).into(target);
        enableEdit(false);
    }

    private void enableEdit(boolean isEdit) {
        this.isEdit = isEdit;
        nameET.setFocusableInTouchMode(isEdit);
        nameET.setEnabled(isEdit);
        phoneET.setFocusableInTouchMode(isEdit);
        phoneET.setEnabled(isEdit);
        picIV.setOnClickListener(isEdit ? this : null);
        updateBT.setText(isEdit ? "Submit" : "Update Info");
        changePassBT.setVisibility(isEdit ? View.GONE : View.VISIBLE);
        if (isEdit) {
            nameET.requestFocus();
        }
    }

    @OnClick(R.id.updateBT)
    public void submit() {
        if (isEdit) {
            if (validate()) {
                updateProfile();
            }
        } else {
            enableEdit(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.picIV:
                if (PermissionsManager.checkPermissions(baseActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 122, this)) {
                    selectPic();
                }
                break;
        }
    }

    @OnClick(R.id.changePassBT)
    public void changePass() {
        baseActivity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, new NewPasswordFragment())
                .addToBackStack(null)
                .commit();
    }

    private void selectPic() {
        new ImageUtils.ImageSelect.Builder(baseActivity, this, 123).aspectRatio(5, 4).start();
    }

    private void updateProfile() {
        RequestBody sessId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID));
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), getText(nameET));
        RequestBody phone = RequestBody.create(MediaType.parse("text/plain"), getText(phoneET));
        MultipartBody.Part image = null;
        try {
            image = MultipartBody.Part.createFormData("pic"
                    , imageFile.getName()
                    , RequestBody.create(MediaType.parse("image/jpeg"), imageFile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateCall = apiInterface.updateProfile(sessId, name, phone, image);
        apiManager.makeApiCall(updateCall, this);
    }

    private boolean validate() {
        if (getText(nameET).isEmpty()) {
            showToast("Please enter Name", true);
        } else if (getText(phoneET).isEmpty()) {
            showToast("Please enter Phone Number", true);
        } else
            return true;
        return false;
    }

    @Override
    public void onSuccess(Call call, Object object) {
        super.onSuccess(call, object);
        if (updateCall == call) {
            JsonObject jsonObject = (JsonObject) object;
            JsonObject data = jsonObject.getAsJsonArray("data").get(0).getAsJsonObject();
            Type objectType = new TypeToken<UserData>() {
            }.getType();


            UserData userDataOld = (UserData) store.getUserData(Const.USER_DATA, UserData.class);
            String compId = userDataOld.cId;

            UserData userData = new Gson().fromJson(data, objectType);
            userData.cId = compId;
            store.saveString(Const.USER_ID, userData.id);
            store.saveUserData(Const.USER_DATA, userData);
            ((MainActivity) baseActivity).setUserData();
            showToast("Profile Updated Successfully");
            baseActivity.onBackPressed();
        }
    }

    @Override
    public void onImageSelected(String imagePath, int resultCode) {
        Bitmap bitmap = ImageUtils.imageCompress(imagePath);
        imageFile = ImageUtils.bitmapToFile(bitmap, baseActivity);
        picIV.setImageBitmap(bitmap);
        log("File Size >>>>>>>>." + imageFile.length());
    }

    @Override
    public void onPermissionsGranted(int resultCode) {
        selectPic();
    }

    @Override
    public void onPermissionsDenied(int resultCode) {

    }
}
