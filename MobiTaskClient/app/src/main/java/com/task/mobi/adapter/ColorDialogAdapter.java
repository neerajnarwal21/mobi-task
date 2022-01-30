package com.task.mobi.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.task.mobi.R;
import com.task.mobi.activity.BaseActivity;
import com.task.mobi.data.ColorData;

import java.util.ArrayList;


public class ColorDialogAdapter extends RecyclerView.Adapter<ColorDialogAdapter.MyViewHolder> {

    BaseActivity baseActivity;
    private ArrayList<ColorData> colorDatas;


    public ColorDialogAdapter(BaseActivity baseActivity, ArrayList<ColorData> colorDatas) {
        this.colorDatas = colorDatas;
        this.baseActivity = baseActivity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_dialog_colorcodes, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ColorData colorData = colorDatas.get(position);
        holder.colorIV.setImageResource(colorData.colorCode);
        holder.colorCodeTV.setText(colorData.codeName);
    }


    @Override
    public int getItemCount() {
        return colorDatas.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView colorCodeTV;
        public ImageView colorIV;

        public MyViewHolder(View view) {
            super(view);
            colorIV = (ImageView) view.findViewById(R.id.colorIV);
            colorCodeTV = (TextView) view.findViewById(R.id.colorCodeTV);
        }
    }
}