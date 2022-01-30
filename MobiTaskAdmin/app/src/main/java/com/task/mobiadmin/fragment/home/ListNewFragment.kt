package com.task.mobiadmin.fragment.home

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.task.mobiadmin.R
import com.task.mobiadmin.activity.MainActivity
import com.task.mobiadmin.adapter.TaskAdapter
import com.task.mobiadmin.retrofitManager.ResponseListener
import com.task.mobiadmin.utils.Const
import kotlinx.android.synthetic.main.fg_list.*
import retrofit2.Call

/**
 * Created by neeraj on 2/4/18.
 */

class ListNewFragment : ListLoadingBaseFragment() {
    private var listener: TabCountListener.NewTabCount? = null
    private var adapter: TaskAdapter? = null
    private lateinit var linearManager:LinearLayoutManager
    private var vieww: View? = null
    var loading = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        if (vieww == null) {
//            vieww = inflater!!.inflate(R.layout.fg_list, container, false) as ViewGroup
//        }
        return inflater!!.inflate(R.layout.fg_list, container, false) as ViewGroup
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        linearManager = LinearLayoutManager(context)
        listRV.layoutManager = linearManager
        listRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItems = linearManager.itemCount
                val lastItem = linearManager.findLastVisibleItemPosition()
                if (totalItems <= lastItem + 1 && !loading && getTasks(Const.TASK_NEW)) {
                    loading = true
                    adapter?.updateLoadMoreView(loading)
                }
            }
        })
        setAdapter()
        getTasks(Const.TASK_NEW)
    }

    private fun setAdapter() {
        adapter = TaskAdapter(taskDatas, baseActivity)
        listRV.adapter = adapter
        adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                emptyTV.visibility = if (adapter?.itemCount == 0) View.VISIBLE else View.GONE
            }
        })
    }

    private var filterText = ""

    fun onQueryTextChange(text: String) {
        adapter?.getFilter()?.filter(text)
        filterText = text
    }

    fun onResetList() {
        adapter?.getFilter()?.filter("")
        filterText = ""
    }

    fun setOnCountListener(listener: TabCountListener.NewTabCount) {
        this.listener = listener
    }

    override fun onSuccess(call: Call<*>?, `object`: Any?) {
        tasksPB.visibility = View.GONE
        loadingTV.visibility = View.GONE
        loading = false
        adapter?.updateLoadMoreView(loading)
        super.onSuccess(call, `object`)

        (baseActivity as MainActivity).newTasks = totalRecords

        if (pageNo == 1)
            setAdapter()
        else {
            adapter?.notifyItemInserted(taskDatas.size)
            adapter?.getFilter()?.filter(filterText)
            if (!filterText.isEmpty() && !loading) {
                loading = true
                getTasks(Const.TASK_NEW)
            }
        }
    }

    override fun onError(call: Call<*>?, statusCode: Int, errorMessage: String?, responseListener: ResponseListener?) {
        tasksPB.visibility = View.GONE
        loadingTV.visibility = View.GONE
        loading = false
        adapter?.updateLoadMoreView(loading)
        if (statusCode == Const.ErrorCodes.NO_TASKS) {
            adapter!!.notifyDataSetChanged()
        } else
            super.onError(call, statusCode, errorMessage, responseListener)
    }
}