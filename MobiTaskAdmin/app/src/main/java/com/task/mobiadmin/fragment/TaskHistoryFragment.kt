package com.task.mobiadmin.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.task.mobiadmin.R
import com.task.mobiadmin.adapter.TaskAdapter
import com.task.mobiadmin.data.TaskData
import com.task.mobiadmin.retrofitManager.ResponseListener
import com.task.mobiadmin.utils.Const
import kotlinx.android.synthetic.main.fg_task_history.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import java.util.*

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class TaskHistoryFragment : BaseFragment() {

    private var pageNum = 0
    private var totalPages = 1
    private var tasksCall: Call<JsonObject>? = null
    private val taskDatas = ArrayList<TaskData>()
    private var linearLayoutManager: LinearLayoutManager? = null
    private var loading = false
    private var taskAdapter: TaskAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbar(true, "Task History")
        return inflater!!.inflate(R.layout.fg_task_history, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        taskDatas.clear()
    }

    private fun initUI() {
        pageNum = 0
        linearLayoutManager = LinearLayoutManager(baseActivity)
        listRV.layoutManager = linearLayoutManager

        listRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItems = linearLayoutManager!!.itemCount
                val lastVisibleItem = linearLayoutManager!!.findLastVisibleItemPosition()
                if (pageNum < totalPages && !loading && totalItems <= lastVisibleItem + 2) {
                    getServerData()
                    loading = true
                    taskAdapter?.updateLoadMoreView(loading)
                }
            }
        })
        setAdapter()
        getServerData()
    }

    private fun getServerData() {
        val sessId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val userType = RequestBody.create(MediaType.parse("text/plain"), Const.COMPANY)
        val pageNo = RequestBody.create(MediaType.parse("text/plain"), pageNum.toString())
        tasksCall = apiInterface.oldTasks(sessId, userType, pageNo)
        apiManager.makeApiCall(tasksCall, this)
    }

    private fun setAdapter() {
        taskAdapter = TaskAdapter(taskDatas, baseActivity)
        listRV.adapter = taskAdapter
    }

    override fun onSuccess(call: Call<*>, `object`: Any) {
        super.onSuccess(call, `object`)
        if (tasksCall === call) {
            emptyTV.visibility = View.GONE
            val jsonObject = `object` as JsonObject
            val data = jsonObject.getAsJsonArray("data")
            val objectType = object : TypeToken<ArrayList<TaskData>>() {}.type
            val datas = Gson().fromJson<ArrayList<TaskData>>(data, objectType)
            totalPages = jsonObject.get("total_pages").asInt
            pageNum++
            taskDatas.addAll(datas)

            loading = false
            taskAdapter?.updateLoadMoreView(loading)

            if (pageNum == 1) setAdapter()
            else taskAdapter?.notifyDataSetChanged()
        }
    }

    override fun onError(call: Call<*>, statusCode: Int, errorMessage: String, responseListener: ResponseListener) {
        if (statusCode == Const.ErrorCodes.NO_TASKS) {
            emptyTV.visibility = View.VISIBLE
            emptyTV.text = "No Tasks Found"
        } else
            super.onError(call, statusCode, errorMessage, responseListener)
        loading = false
        taskAdapter?.updateLoadMoreView(loading)
    }
}
