package com.task.mobi.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.task.mobi.R;
import com.task.mobi.adapter.TestAdapter;
import com.task.mobi.data.TestLocationData;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
public class TestFragment extends BaseFragment {

    public View view;

    @BindView(R.id.listRV)
    RecyclerView listRV;


    private ArrayList<TestLocationData> taskDatas = new ArrayList<>();
    private TestAdapter taskAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fg_task_history, null);
        }
        setToolbar(true, "Task History");
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
    }

    private void initUI() {
        listRV.setLayoutManager(new LinearLayoutManager(baseActivity));
        if (store.getData("locDatas") != null) {
            taskDatas = store.getData("locDatas");
        }
        taskAdapter = new TestAdapter(baseActivity, taskDatas);
        listRV.setAdapter(taskAdapter);
    }

}
