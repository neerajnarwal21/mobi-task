package com.task.mobi.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.task.mobi.R;
import com.task.mobi.activity.MainActivity;
import com.task.mobi.adapter.ColorDialogAdapter;
import com.task.mobi.adapter.TaskAdapter;
import com.task.mobi.data.ColorData;
import com.task.mobi.data.TaskData;
import com.task.mobi.data.UserData;
import com.task.mobi.retrofitManager.ResponseListener;
import com.task.mobi.utils.AppUtils;
import com.task.mobi.utils.Const;
import com.task.mobi.utils.Utils;

import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
public class HomeFragment extends BaseFragment {

    public View view;

    @BindView(R.id.listRV)
    RecyclerView listRV;

    @BindView(R.id.userIV)
    ImageView userIV;

    @BindView(R.id.collapseTL)
    CollapsingToolbarLayout collapseTL;

    @BindView(R.id.emptyTV)
    TextView emptyTV;

    @BindView(R.id.loadingIL)
    ConstraintLayout loadingIL;

    private int pageNum = 1, totalPages = 1;
    private Call<JsonObject> tasksCall;
    private ArrayList<TaskData> taskDatas = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private boolean loading = false;
    private TaskAdapter taskAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fg_home, null);
        }
//        Utils.setStatusBarTranslucent(baseActivity, true);
        setToolbar(false, "");
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
        taskDatas.clear();
    }

    @OnClick(R.id.drawerIV)
    public void openDrawer() {
        ((MainActivity) baseActivity).drawer.openDrawer(GravityCompat.START);
    }

    @OnClick(R.id.alertIV)
    public void showColorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(baseActivity);
        View view = View.inflate(baseActivity, R.layout.dialog_colorcode, null);
        RecyclerView listRV = (RecyclerView) view.findViewById(R.id.listRV);
        listRV.setLayoutManager(new LinearLayoutManager(baseActivity));
        ArrayList<ColorData> colorDatas = createList();
        listRV.setAdapter(new ColorDialogAdapter(baseActivity, colorDatas));
        builder.setTitle("Task color codes");
        builder.setView(view);
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private ArrayList<ColorData> createList() {
        ArrayList<ColorData> colorDatas = new ArrayList<>();
        colorDatas.add(new ColorData("New Task", R.color.colorPrimary));
        colorDatas.add(new ColorData("Under Progress", R.color.DarkSlateGray));
        colorDatas.add(new ColorData("Completed", R.color.ForestGreen));
        return colorDatas;
    }

    private void initUI() {
        UserData userData = (UserData) store.getUserData(Const.USER_DATA, UserData.class);
        AppUtils.MyTarget target = new AppUtils.MyTarget(userIV);
        baseActivity.picasso.load(Const.ASSETS_BASE_URL + userData.photo).error(R.mipmap.ic_default_user).placeholder(R.mipmap.ic_default_user).into(target);
        userIV.setTag(target);

        linearLayoutManager = new LinearLayoutManager(baseActivity);
        listRV.setLayoutManager(linearLayoutManager);
        taskAdapter = new TaskAdapter(baseActivity, taskDatas);
        listRV.setAdapter(taskAdapter);

        listRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItems = linearLayoutManager.getItemCount();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (pageNum < totalPages && !loading && totalItems <= lastVisibleItem + 2) {
                    loading = true;
                    pageNum++;
                    getServerData();
                }
            }
        });

        getServerData();
    }

    private void getServerData() {
        RequestBody sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID));
        RequestBody pageNo = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(pageNum));
        RequestBody deviceToken = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.DEVICE_TOKEN));
        tasksCall = apiInterface.getTasks(sessionId, pageNo, deviceToken);
        apiManager.makeApiCall(tasksCall, false, this);
    }

    @Override
    public void onSuccess(Call call, Object object) {
        super.onSuccess(call, object);
        if (tasksCall == call) {
            collapseTL.setTitleEnabled(true);
            emptyTV.setVisibility(View.GONE);
            loadingIL.setVisibility(View.GONE);
            JsonObject jsonObject = (JsonObject) object;
            JsonArray data = jsonObject.getAsJsonArray("Data");
            Type objectType = new TypeToken<ArrayList<TaskData>>() {
            }.getType();
            ArrayList<TaskData> datas = new Gson().fromJson(data, objectType);
            totalPages = jsonObject.get("total").getAsInt();
            pageNum = jsonObject.get("pageno").getAsInt();
            taskDatas.addAll(datas);
            taskAdapter.notifyDataSetChanged();
            for (TaskData taskData : datas) {
                if (taskData.status == Const.TASK_START) {
                    Utils.startService(baseActivity, taskData.id, true);
                }
            }
        }
    }

    @Override
    public void onError(Call call, int statusCode, String errorMessage, ResponseListener responseListener) {
        loadingIL.setVisibility(View.GONE);
        if (statusCode == Const.ErrorCodes.NO_TASKS) {
            emptyTV.setVisibility(View.VISIBLE);
            emptyTV.setText("No tasks");
            collapseTL.setTitleEnabled(false);
        } else
            super.onError(call, statusCode, errorMessage, responseListener);
    }
}
