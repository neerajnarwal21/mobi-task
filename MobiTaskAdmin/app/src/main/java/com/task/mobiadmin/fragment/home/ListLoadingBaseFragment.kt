package com.task.mobiadmin.fragment.home

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.task.mobiadmin.data.TaskData
import com.task.mobiadmin.fragment.BaseFragment
import com.task.mobiadmin.retrofitManager.ResponseListener
import com.task.mobiadmin.utils.Const
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call

/**
 * Created by neeraj on 2/4/18.
 */

open class ListLoadingBaseFragment : BaseFragment() {
    var taskDatas = ArrayList<TaskData>()
    private var taskCall: Call<JsonObject>? = null
    var pageNo = 0
    private var totalPages = 1
    var totalRecords = 0

    fun getTasks(taskState: Int): Boolean {
        if (!activity.isFinishing) {
            return if (pageNo < totalPages) {
                val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
                val pageNo = RequestBody.create(MediaType.parse("text/plain"), pageNo.toString())
                val token = RequestBody.create(MediaType.parse("text/plain"), baseActivity.store.getString(Const.DEVICE_TOKEN))
                val tState = RequestBody.create(MediaType.parse("text/plain"), taskState.toString())
                taskCall = apiInterface.taskList(sessionId, pageNo, tState, token)
                apiManager.makeApiCall(taskCall, false, this)
                true
            } else false
        }
        return false
    }

    override fun onSuccess(call: Call<*>?, `object`: Any?) {
        super.onSuccess(call, `object`)
        if (taskCall === call) {
            val jsonObject = `object` as JsonObject
            pageNo++
            totalPages = jsonObject.get("total_pages").asInt
            totalRecords = jsonObject.get("total_record").asInt
            val jasonData = jsonObject.getAsJsonArray("data")
            val objectType = object : TypeToken<ArrayList<TaskData>>() {}.type
            val datas = Gson().fromJson<ArrayList<TaskData>>(jasonData, objectType)
            taskDatas.addAll(datas)
        }
    }

    override fun onError(call: Call<*>?, statusCode: Int, errorMessage: String?, responseListener: ResponseListener?) {
        if (statusCode != Const.SOCKET_TIMEOUT)
            super.onError(call, statusCode, errorMessage, responseListener)
    }
}