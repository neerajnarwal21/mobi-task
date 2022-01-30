package com.task.mobi.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.task.mobi.R;
import com.task.mobi.activity.BaseActivity;
import com.task.mobi.data.TestLocationData;

import java.util.ArrayList;


public class TestAdapter extends RecyclerView.Adapter<TestAdapter.MyViewHolder> {

    BaseActivity baseActivity;
    private ArrayList<TestLocationData> taskDatas;


    public TestAdapter(BaseActivity baseActivity, ArrayList<TestLocationData> taskDatas) {
        this.taskDatas = taskDatas;
        this.baseActivity = baseActivity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_test, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        TestLocationData taskData = taskDatas.get(position);
        holder.taskNoTV.setText(baseActivity.getString(R.string.task_num, position + 1));
        holder.timeTV.setText(taskData.date);
        holder.addressTV.setText(taskData.location);
        holder.locationTV.setText(taskData.lat + " , " + taskData.lng);
    }


    @Override
    public int getItemCount() {
        return taskDatas.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView taskNoTV, timeTV, locationTV, addressTV;

        public MyViewHolder(View view) {
            super(view);
            taskNoTV = (TextView) view.findViewById(R.id.taskNoTV);
            timeTV = (TextView) view.findViewById(R.id.timeTV);
            locationTV = (TextView) view.findViewById(R.id.locationTV);
            addressTV = (TextView) view.findViewById(R.id.addressTV);
        }
    }
}