package com.task.mobi.fragment

import android.os.Bundle
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.task.mobi.R
import com.task.mobi.activity.MainActivity
import com.task.mobi.data.TaskData
import com.task.mobi.data.UserData
import com.task.mobi.utils.AppUtils
import com.task.mobi.utils.Const
import com.task.mobi.utils.Utils
import com.task.mobiadmin.fragment.home.TaskCommentsFragment
import kotlinx.android.synthetic.main.fg_task_history_detail.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class TaskHistoryDetailFragment : BaseFragment() {
    internal lateinit var taskData: TaskData
    internal lateinit var taskId: String
    private var taskCall: Call<JsonObject>? = null
    private lateinit var googleMap: GoogleMap
    private var vieww: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null && arguments.containsKey("taskId")) {
            taskId = arguments.getString("taskId")
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        if (vieww != null) {
            val parent = vieww!!.getParent() as ViewGroup?
            parent?.removeView(vieww)
        }
        try {
            vieww = inflater!!.inflate(R.layout.fg_task_history_detail, null)
        } catch (e: InflateException) {
            e.printStackTrace()
        }
        return vieww
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentSV.visibility = View.GONE
        loadData()
    }

    private fun loadData() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val currentUserType = RequestBody.create(MediaType.parse("text/plain"), Const.TYPE_USER)
        val taskId = RequestBody.create(MediaType.parse("text/plain"), taskId)
        taskCall = apiInterface.singleTask(sessionId, currentUserType, taskId)
        apiManager.makeApiCall(taskCall, this)
    }


    private fun initUI() {
        parentSV.visibility = View.VISIBLE
        setToolbar(true, "Task Detail")
        taskHeadTV.text = taskData.heading
        taskDescTV.text = taskData.taskDescription
        inTimeTV.text = getString(R.string.start_time, Utils.changeDateFormat(taskData.inTime, "yyyy-MM-dd HH:mm:ss", "dd/MM/yy hh:mm a"))
        outTimeTV.text = getString(R.string.end_time, Utils.changeDateFormat(taskData.outTime, "yyyy-MM-dd HH:mm:ss", "dd/MM/yy hh:mm a"))
        AppUtils().loadStaticMap(baseActivity, locIV, taskData.latitude, taskData.longitude, true)
        taskAddressTV.text = taskData.address

        signInTimeTV.text = baseActivity.getString(R.string.signin_time, Utils.changeDateFormat(Utils.getCurrentTimeZoneTime(taskData.taskSignData.signindate, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss", "dd/MM/yy hh:mm a"))
        AppUtils().loadStaticMap(baseActivity, signInlocIV, taskData.taskSignData.signinLat, taskData.taskSignData.signinLong, true)
        val target = AppUtils.MyTarget(signInPicIV)
        baseActivity.picasso.load(Const.ASSETS_BASE_URL + taskData.taskSignData.signinpicture).error(R.mipmap.ic_default_user).placeholder(R.mipmap.ic_default_user).into(target)
        signInPicIV.tag = target
        signOutTimeTV.text = baseActivity.getString(R.string.signout_time, Utils.changeDateFormat(Utils.getCurrentTimeZoneTime(taskData.taskSignData.signoutdate, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss", "dd/MM/yy hh:mm a"))
        AppUtils().loadStaticMap(baseActivity, signOutlocIV, taskData.taskSignData.signoutLat, taskData.taskSignData.signoutLong, true)
        val tgt = AppUtils.MyTarget(signOutPicIV)
        baseActivity.picasso.load(Const.ASSETS_BASE_URL + taskData.taskSignData.signoutpicture).error(R.mipmap.ic_default_user).placeholder(R.mipmap.ic_default_user).into(tgt)
        signOutPicIV.tag = tgt
        workDoneTV.text = taskData.taskSignData.workdone
        signInAddressTV.text = taskData.taskSignData.signinAddress
        signOutAddressTV.text = taskData.taskSignData.signoutAddress
        setupCommentView()
    }

    private fun setupCommentView() {
        (baseActivity as MainActivity).commentsL.visibility = View.VISIBLE
        if (taskData.unreadMsg > 0) {
            (baseActivity as MainActivity).messageCountsTV.visibility = View.VISIBLE
            (baseActivity as MainActivity).messageCountsTV.text = taskData.unreadMsg.toString()
        }
        (baseActivity as MainActivity).commentsL.setOnClickListener({
            val userData = store.getUserData(Const.USER_DATA, UserData::class.java) as UserData
            val toId = userData.cId
            val taskId = taskData.id

            val bndl = Bundle()
            bndl.putString("taskId", taskId)
            bndl.putString("toId", toId)
            if (taskData.status == Const.TASK_APPROVED)
                bndl.putBoolean("isApproved", true)
            val frag = TaskCommentsFragment()
            frag.arguments = bndl
            baseActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_container, frag)
                    .addToBackStack(null)
                    .commit()
        })
    }

    private fun initializeMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            googleMap.uiSettings.setAllGesturesEnabled(false)
            googleMap.uiSettings.isMapToolbarEnabled = false
            googleMap.setOnMarkerClickListener { true }
            this@TaskHistoryDetailFragment.googleMap = googleMap
            MapPathLoad(this@TaskHistoryDetailFragment.googleMap, baseActivity, clickV, emptyTV, taskData)
        }
    }

    override fun onSuccess(call: Call<*>, `object`: Any) {
        super.onSuccess(call, `object`)
        if (taskCall != null && taskCall === call) {
            val jsonObject = `object` as JsonObject
            val jsonData = jsonObject.getAsJsonObject("data")
            val objectType = object : TypeToken<TaskData>() {}.type
            taskData = Gson().fromJson<TaskData>(jsonData, objectType)
            initUI()
            initializeMap()
        }
    }
}
