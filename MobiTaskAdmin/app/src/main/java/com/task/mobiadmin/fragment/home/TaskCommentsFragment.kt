package com.task.mobiadmin.fragment.home

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.task.mobiadmin.R
import com.task.mobiadmin.adapter.CommentsAdapter
import com.task.mobiadmin.data.CommentsData
import com.task.mobiadmin.data.UserData
import com.task.mobiadmin.fragment.BaseFragment
import com.task.mobiadmin.retrofitManager.ResponseListener
import com.task.mobiadmin.utils.Const
import kotlinx.android.synthetic.main.fg_task_comments.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class TaskCommentsFragment : BaseFragment() {

    private var pageNum = 0
    private var totalPages = 1
    private var loading = false
    private lateinit var taskId: String
    private lateinit var toId: String
    private var msgCall: Call<JsonObject>? = null
    private var getCall: Call<JsonObject>? = null
    private var updateCall: Call<JsonObject>? = null
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var adapter: CommentsAdapter? = null
    private val commentsDatas = ArrayList<CommentsData>()
    private var isApproved = false
    private var isSending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null && arguments.containsKey("taskId")) {
            taskId = arguments.getString("taskId")
            toId = arguments.getString("toId")
            if (arguments.containsKey("isApproved"))
                isApproved = arguments.getBoolean("isApproved")
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbar(true, "Comments")
        return inflater!!.inflate(R.layout.fg_task_comments, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pageNum = 0
        initUI()
    }

    private fun getMessages() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val taskId = RequestBody.create(MediaType.parse("text/plain"), taskId)
        val curentUserType = RequestBody.create(MediaType.parse("text/plain"), Const.TYPE_COMPANY)
        val pageNo = RequestBody.create(MediaType.parse("text/plain"), pageNum.toString())
        getCall = apiInterface.getMessages(sessionId, taskId, curentUserType, pageNo)
        apiManager.makeApiCall(getCall, false, this)
    }

    private fun updateLastMsgOnServer() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val currentUserType = RequestBody.create(MediaType.parse("text/plain"), Const.TYPE_COMPANY)
        val taskId = RequestBody.create(MediaType.parse("text/plain"), taskId)
        updateCall = apiInterface.updateLastMessage(sessionId, taskId, currentUserType)
        apiManager.makeApiCall(updateCall, false, null)
    }

    private fun initUI() {
        linearLayoutManager = LinearLayoutManager(baseActivity)
        linearLayoutManager.reverseLayout = true
        listRV.layoutManager = linearLayoutManager
        listRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItems = linearLayoutManager.itemCount
                val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                if (pageNum < totalPages && !loading && totalItems <= lastVisibleItem + 2) {
                    loading = true
                    getMessages()
                    adapter?.updateLoadMoreView(loading)
                }
            }
        })
        setAdapter()
        updateLastMsgOnServer()
        getMessages()

        sendIV.setOnClickListener {
            if (getText(msgET).isEmpty()) {
                showToast("Message can not be blank")
            } else {
                sendMessage()
                switchVisibility(true)
            }
        }
        if (isApproved) {
            bottomCL.visibility = View.GONE
        }
    }

    private fun setAdapter() {
        adapter = CommentsAdapter(commentsDatas)
        listRV.adapter = adapter
    }

    private fun switchVisibility(isSending: Boolean) {
        this.isSending = isSending
        sendIV.visibility = if (isSending) View.INVISIBLE else View.VISIBLE
        sendPB.visibility = if (isSending) View.VISIBLE else View.GONE
    }

    private fun sendMessage() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val msgTo = RequestBody.create(MediaType.parse("text/plain"), toId)
        val msgToType = RequestBody.create(MediaType.parse("text/plain"), Const.TYPE_USER)
        val msg = RequestBody.create(MediaType.parse("text/plain"), getText(msgET))
        val taskId = RequestBody.create(MediaType.parse("text/plain"), taskId)

        msgET.setText("")
        msgCall = apiInterface.sendMessage(sessionId, msgTo, msgToType, msg, taskId)
        apiManager.makeApiCall(msgCall, false, this)
    }

    override fun onResume() {
        super.onResume()
        val mNotificationManager = baseActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(1)
        store.setBoolean(Const.CHAT_FOREGROUND, true)
        LocalBroadcastManager.getInstance(context).registerReceiver(onNewMessage, IntentFilter(Const.NEW_MESSAGE_BROADCAST))
    }

    override fun onDestroy() {
        super.onDestroy()
        store.setBoolean(Const.CHAT_FOREGROUND, false)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(onNewMessage)
    }

    override fun onSuccess(call: Call<*>?, `object`: Any?) {
        super.onSuccess(call, `object`)
        if (getCall != null && getCall === call) {
            emptyTV.visibility = View.GONE
            if (isSending) switchVisibility(false)
            val jsonObject = `object` as JsonObject
            val data = jsonObject.getAsJsonArray("data")
            val objectType = object : TypeToken<ArrayList<CommentsData>>() {}.type
            val datas = Gson().fromJson<ArrayList<CommentsData>>(data, objectType)
            totalPages = jsonObject.get("total_pages").asInt
            pageNum++

            val userData = store.getUserData(Const.USER_DATA, UserData::class.java) as UserData
            for (d in datas) {
                if (d.mFrom == userData.id) {
                    d.msgDirection = Const.MSG_RIGHT
                    d.fromName = "Me"
                } else
                    d.msgDirection = Const.MSG_LEFT
            }

            var shouldScrollToBottom = true
            if (loading) shouldScrollToBottom = false
            loading = false
            adapter?.updateLoadMoreView(loading)

            commentsDatas.addAll(datas)
            if (pageNum == 1)
                setAdapter()
            else
                adapter?.notifyItemInserted(commentsDatas.size)
            if (shouldScrollToBottom) listRV.scrollToPosition(0)
        } else if (msgCall != null && msgCall === call) {
            pageNum = 0
            commentsDatas.clear()
            updateLastMsgOnServer()
            getMessages()
        }
    }

    override fun onError(call: Call<*>?, statusCode: Int, errorMessage: String?, responseListener: ResponseListener?) {
        when (statusCode) {
            Const.ErrorCodes.NO_COMMENTS ->
                emptyTV.text = if (isApproved) "No Comments on this task" else "No Comments yet\n\nStart a conversation on this task"
            Const.ErrorCodes.UNABLE_TO_SEND_MESSAGE -> {
                showToast("Not allowed to post comment on approved task")
                bottomCL.visibility = View.GONE
            }
            else -> {
                emptyTV.visibility = View.GONE
                if (msgCall != null && msgCall === call && isSending) switchVisibility(false)
                super.onError(call, statusCode, errorMessage, responseListener)
            }
        }
    }

    private var onNewMessage: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            pageNum = 0
            commentsDatas.clear()
            updateLastMsgOnServer()
            getMessages()
        }
    }
}