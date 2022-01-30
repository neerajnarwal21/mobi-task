package com.task.mobiadmin.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.task.mobiadmin.R
import com.task.mobiadmin.data.CommentsData
import com.task.mobiadmin.utils.Const
import com.task.mobiadmin.utils.Utils

class CommentsAdapter(private var datas: ArrayList<CommentsData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val LEFT = 0
    val RIGHT = 1
    val LOADING = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder
        if (viewType == LEFT) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_chat_left, parent, false)
            viewHolder = MyViewHolderLeft(itemView)
        } else if (viewType == RIGHT) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_chat_right, parent, false)
            viewHolder = MyViewHolderLeft(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.inc_load_more, parent, false)
            viewHolder = ProgressViewHolder(itemView)
        }
        return viewHolder
    }

    override fun getItemViewType(position: Int): Int {
        val type: Int
        if (datas.get(position).id.equals("-1"))
            type = LOADING
        else if (datas.get(position).msgDirection.equals(Const.MSG_LEFT))
            type = LEFT
        else
            type = RIGHT
        return type
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = datas.get(position);
        if (holder is MyViewHolderLeft) {
            holder.nameTV.text = data.fromName
            holder.msgTV.text = data.message
            holder.timeTV.text = Utils.changeDateFormat(
                    Utils.getCurrentTimeZoneTime(data.mTime, "yyyy-MM-dd HH:mm:ss"),
                    "yyyy-MM-dd HH:mm:ss",
                    "d MMM, hh:mma")
        } else if (holder is MyViewHolderRight) {
            holder.nameTV.text = data.toName
            holder.msgTV.text = data.message
            holder.timeTV.text = Utils.changeDateFormat(
                    Utils.getCurrentTimeZoneTime(data.mTime, "yyyy-MM-dd HH:mm:ss"),
                    "yyyy-MM-dd HH:mm:ss",
                    "d MMM, hh:mma")
        }
    }

    fun updateLoadMoreView(isLoading: Boolean) {
        if (isLoading) {
            datas.add(CommentsData("-1"))
            notifyItemInserted(datas.size - 1)
        } else {
            if (datas.size > 0 && datas.get(datas.size - 1).id.equals("-1")) {
                datas.removeAt(datas.size - 1)
                notifyItemRemoved(datas.size)
            }
        }
    }

    override fun getItemCount() = datas.size

    inner class MyViewHolderLeft(view: View) : RecyclerView.ViewHolder(view) {
        internal var nameTV: TextView
        internal var msgTV: TextView
        internal var timeTV: TextView

        init {
            nameTV = view.findViewById(R.id.nameTV)
            msgTV = view.findViewById(R.id.msgTV)
            timeTV = view.findViewById(R.id.timeTV)
        }
    }

    inner class MyViewHolderRight(view: View) : RecyclerView.ViewHolder(view) {
        internal var nameTV: TextView
        internal var msgTV: TextView
        internal var timeTV: TextView

        init {
            nameTV = view.findViewById(R.id.nameTV)
            msgTV = view.findViewById(R.id.msgTV)
            timeTV = view.findViewById(R.id.timeTV)
        }
    }

    inner class ProgressViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal var loadingMorePB: ProgressBar
        internal var loadingTV: TextView

        init {
            loadingMorePB = view.findViewById(R.id.loadingMorePB)
            loadingTV = view.findViewById(R.id.loadingTV)
        }
    }
}