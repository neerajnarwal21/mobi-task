package com.task.mobi.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.task.mobi.R;
import com.task.mobi.activity.BaseActivity;
import com.task.mobi.data.TaskData;
import com.task.mobi.fragment.TaskHistoryDetailFragment;
import com.task.mobi.utils.Utils;

import java.util.ArrayList;


public class TaskHistoryAdapter extends RecyclerView.Adapter<TaskHistoryAdapter.MyViewHolder> {

    BaseActivity baseActivity;
    private ArrayList<TaskData> taskDatas;


    public TaskHistoryAdapter(BaseActivity baseActivity, ArrayList<TaskData> taskDatas) {
        this.taskDatas = taskDatas;
        this.baseActivity = baseActivity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_task_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        TaskData taskData = taskDatas.get(position);
        holder.inTimeTV.setText(baseActivity.getString(R.string.start_time, Utils.changeDateFormat(taskData.inTime, "yyyy-MM-dd HH:mm:ss", "dd/MM/yy hh:mm a")));
        holder.outTimeTV.setText(baseActivity.getString(R.string.end_time, Utils.changeDateFormat(taskData.outTime, "yyyy-MM-dd HH:mm:ss", "dd/MM/yy hh:mm a")));
        holder.taskNoTV.setText(baseActivity.getString(R.string.task_num, position + 1));
        holder.taskNoTV.setBackgroundColor(ContextCompat.getColor(baseActivity, R.color.ForestGreen));
        holder.locationTV.setTextColor(ContextCompat.getColor(baseActivity, R.color.ForestGreen));
        holder.taskHeadTV.setText(taskData.heading);
        holder.addressTV.setText(taskData.address);

        holder.msgCountTV.setVisibility(taskData.unreadMsg > 0 ? View.VISIBLE : View.GONE);
        holder.msgCountTV.setText("" + taskData.unreadMsg);

        holder.mainCV.setTag(taskData);
        holder.mainCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskData data = (TaskData) v.getTag();
                Fragment fragment = new TaskHistoryDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString("taskId", data.id);
                fragment.setArguments(bundle);
                baseActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }


    @Override
    public int getItemCount() {
        return taskDatas.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView inTimeTV, outTimeTV, taskNoTV, taskHeadTV, locationTV, addressTV, msgCountTV;
        public CardView mainCV;

        public MyViewHolder(View view) {
            super(view);
            mainCV = view.findViewById(R.id.mainCV);
            inTimeTV = view.findViewById(R.id.inTimeTV);
            outTimeTV = view.findViewById(R.id.outTimeTV);
            locationTV = view.findViewById(R.id.locationTV);
            taskNoTV = view.findViewById(R.id.taskNoTV);
            taskHeadTV = view.findViewById(R.id.taskHeadTV);
            addressTV = view.findViewById(R.id.addressTV);
            msgCountTV = view.findViewById(R.id.msgCountTV);
        }
    }
}