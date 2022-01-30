package com.task.mobiadmin.adapter

import android.os.Bundle
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ProgressBar
import android.widget.TextView
import com.makeramen.roundedimageview.RoundedImageView
import com.task.mobiadmin.R
import com.task.mobiadmin.activity.BaseActivity
import com.task.mobiadmin.data.TaskData
import com.task.mobiadmin.fragment.home.TaskDetailFragment
import com.task.mobiadmin.utils.Const
import com.task.mobiadmin.utils.Utils


class TaskAdapter(private var taskDatas: ArrayList<TaskData>, private val baseActivity: BaseActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data: ArrayList<TaskData>? = null

    init {
        data = taskDatas
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder
        if (viewType == 0) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_task_list, parent, false)
            viewHolder = MyViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.inc_load_more, parent, false)
            viewHolder = ProgressViewHolder(itemView)
        }
        return viewHolder
    }

    override fun getItemViewType(position: Int) = if (taskDatas.get(position).id.equals("-1")) 1 else 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = taskDatas.get(position)
        if (holder is MyViewHolder) {
            holder.titleTV.text = data.heading
            holder.descTV.text = data.taskDescription
            holder.msgCountTV.visibility = if (data.unreadMsg > 0) View.VISIBLE else View.GONE
            holder.msgCountTV.text = data.unreadMsg.toString()
            holder.nameTV.text = data.empData.name
            holder.deadLineTV.text = Utils.changeDateFormat(data.outTime, "yyyy-MM-dd HH:mm:ss", "d MMM")
            baseActivity.picasso.load(Const.ASSETS_BASE_URL + data.empData.photo).placeholder(R.mipmap.ic_default_user).into(holder.userRIV)
            holder.mainCV.setOnClickListener {
                val frag = TaskDetailFragment()
                val bundle = Bundle()
                bundle.putString("taskId", data.id)
                frag.arguments = bundle
                baseActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_container, frag)
                        .commit()
            }
        }
    }

    fun updateLoadMoreView(isLoading: Boolean) {
        if (isLoading) {
            taskDatas.add(TaskData("-1"))
            notifyItemInserted(taskDatas.size - 1)
        } else {
            if (taskDatas.size > 0 && taskDatas.get(taskDatas.size - 1).id.equals("-1")) {
                taskDatas.removeAt(taskDatas.size - 1)
                notifyItemRemoved(taskDatas.size)
            }
        }
    }

    override fun getItemCount() = taskDatas.size

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal var userRIV: RoundedImageView
        internal var titleTV: TextView
        internal var descTV: TextView
        internal var msgCountTV: TextView
        internal var nameTV: TextView
        internal var deadLineTV: TextView
        internal var mainCV: CardView

        init {
            userRIV = view.findViewById(R.id.userRIV)
            titleTV = view.findViewById(R.id.titleTV)
            descTV = view.findViewById(R.id.descTV)
            msgCountTV = view.findViewById(R.id.msgCountTV)
            nameTV = view.findViewById(R.id.nameTV)
            deadLineTV = view.findViewById(R.id.deadLineTV)
            mainCV = view.findViewById(R.id.mainCV)
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

    fun getFilter() = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            val tempFliteredDataList = ArrayList<TaskData>()
            if (constraint == null || constraint.toString().trim { it <= ' ' }.isEmpty()) {
                results.values = data
            } else {
                val constrainString = constraint.toString().toLowerCase()
                for (post in data!!) {
                    if (post.heading.toLowerCase().contains(constrainString)) {
                        tempFliteredDataList.add(post)
                    }
                    if (post.empData.name.toLowerCase().contains(constrainString)) {
                        tempFliteredDataList.add(post)
                    }
                }
                results.values = tempFliteredDataList
            }
            return results
        }

        override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
            try {
                if (results.values != null) {
                    taskDatas = results.values as ArrayList<TaskData>
                    notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}