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
import com.task.mobi.fragment.TaskDetailFragment;
import com.task.mobi.fragment.TaskHistoryDetailFragment;
import com.task.mobi.utils.Const;
import com.task.mobi.utils.Utils;

import java.util.ArrayList;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder> {

    private BaseActivity baseActivity;
    private ArrayList<TaskData> taskDatas;


    public TaskAdapter(BaseActivity baseActivity, ArrayList<TaskData> taskDatas) {
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
        holder.inTimeTV.setText(baseActivity.getString(R.string.start_time, Utils.changeDateFormat(taskData.inTime, "yyyy-MM-dd HH:mm:ss", "dd MMM, hh:mm a")));
        holder.outTimeTV.setText(baseActivity.getString(R.string.end_time, Utils.changeDateFormat(taskData.outTime, "yyyy-MM-dd HH:mm:ss", "dd MMM, hh:mm a")));
        holder.taskNoTV.setText(baseActivity.getString(R.string.task_num, position + 1));
        holder.taskHeadTV.setText(taskData.heading);
        holder.addressTV.setText(taskData.address);
        holder.msgCountTV.setVisibility(taskData.unreadMsg > 0 ? View.VISIBLE : View.GONE);
        holder.msgCountTV.setText("" + taskData.unreadMsg);
        holder.mainCV.setTag(taskData);
        setColors(holder, taskData.status);
        holder.mainCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskData data = (TaskData) v.getTag();
                Fragment fragment;
                if (data.status == Const.TASK_COMPLETE)
                    fragment = new TaskHistoryDetailFragment();
                else
                    fragment = new TaskDetailFragment();
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

    private void setColors(MyViewHolder holder, int status) {
        switch (status) {
            case Const.TASK_START:
                holder.taskNoTV.setBackgroundColor(ContextCompat.getColor(baseActivity, R.color.DarkSlateGray));
                holder.locationTV.setTextColor(ContextCompat.getColor(baseActivity, R.color.DarkSlateGray));
                break;
            case Const.TASK_COMPLETE:
                holder.taskNoTV.setBackgroundColor(ContextCompat.getColor(baseActivity, R.color.ForestGreen));
                holder.locationTV.setTextColor(ContextCompat.getColor(baseActivity, R.color.ForestGreen));
                break;
            default:
                holder.taskNoTV.setBackgroundColor(ContextCompat.getColor(baseActivity, R.color.colorPrimary));
                holder.locationTV.setTextColor(ContextCompat.getColor(baseActivity, R.color.colorPrimary));
                break;
        }
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
            taskNoTV = view.findViewById(R.id.taskNoTV);
            taskHeadTV = view.findViewById(R.id.taskHeadTV);
            locationTV = view.findViewById(R.id.locationTV);
            addressTV = view.findViewById(R.id.addressTV);
            msgCountTV = view.findViewById(R.id.msgCountTV);
        }
    }
}