package com.task.mobi.adapter;

import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.task.mobi.R;
import com.task.mobi.activity.BaseActivity;
import com.task.mobi.data.RouteData;
import com.task.mobi.fragment.RouteDetailFragment;
import com.task.mobi.utils.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class RouteDialogAdapter extends RecyclerView.Adapter<RouteDialogAdapter.MyViewHolder> {

    BaseActivity baseActivity;
    private ArrayList<RouteData> routeDatas;
    private RouteDetailFragment routeDetailFragment;


    public RouteDialogAdapter(BaseActivity baseActivity, ArrayList<RouteData> routeDatas, RouteDetailFragment routeDetailFragment) {
        this.routeDatas = routeDatas;
        this.baseActivity = baseActivity;
        this.routeDetailFragment = routeDetailFragment;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_dialog_route, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        RouteData routeData = routeDatas.get(position);
        if (position == 0) {
            holder.topV.setVisibility(View.GONE);
            holder.bottomV.setVisibility(View.VISIBLE);
            holder.centerIV.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_round_green));
        } else if (position == routeDatas.size() - 1) {

            holder.bottomV.setVisibility(View.GONE);
            holder.topV.setVisibility(View.VISIBLE);
            holder.centerIV.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_round_red));
        } else {
            holder.topV.setVisibility(View.VISIBLE);
            holder.bottomV.setVisibility(View.VISIBLE);
            holder.centerIV.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_round_blue));
        }
        holder.locationTV.setText(routeData.address);
        holder.dateTimeTV.setText(Utils.changeDateFormat(Utils.getCurrentTimeZoneTime(routeData.date, "yyyy-MM-dd HH:mm:ss"),"yyyy-MM-dd HH:mm:ss", "dd MMM, hh:mm a"));
        if (position == 0) {
            holder.lengthTV.setText("Start point");
        } else if (position <= routeDatas.size() - 1) {
            float[] floats = new float[5];
            Location.distanceBetween(routeDatas.get(position - 1).lat, routeDatas.get(position - 1).lng
                    , routeData.lat, routeData.lng
                    , floats);
            if (floats[0] != 0.0) {
                DecimalFormat df = new DecimalFormat("###.#");
                df.format(floats[0] / 1000);
                holder.lengthTV.setText(df.format(floats[0] / 1000) + " Km");
            } else {
                holder.lengthTV.setText("0 Km");
            }
        }
        holder.parentLL.setTag(position);
        holder.parentLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.getTag();
                if (pos == 0) {
                    routeDetailFragment.focusMarker(routeDatas.get(0));
                } else if (pos == routeDatas.size() - 1) {
                    routeDetailFragment.focusMarker(routeDatas.get(routeDatas.size() - 1));
                } else {
                    routeDetailFragment.onLocationClick(routeDatas.get(pos));
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return routeDatas.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView locationTV, lengthTV, dateTimeTV;
        public ImageView centerIV;
        public View bottomV, topV;
        public LinearLayout parentLL;

        public MyViewHolder(View view) {
            super(view);
            parentLL = (LinearLayout) view.findViewById(R.id.parentLL);
            centerIV = (ImageView) view.findViewById(R.id.centerIV);
            dateTimeTV = (TextView) view.findViewById(R.id.dateTimeTV);
            locationTV = (TextView) view.findViewById(R.id.locationTV);
            lengthTV = (TextView) view.findViewById(R.id.lengthTV);
            topV = view.findViewById(R.id.topV);
            bottomV = view.findViewById(R.id.bottomV);
        }
    }
}