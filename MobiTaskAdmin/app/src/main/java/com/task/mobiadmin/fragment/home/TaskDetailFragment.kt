package com.task.mobiadmin.fragment.home

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AlertDialog
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.task.mobiadmin.R
import com.task.mobiadmin.activity.MainActivity
import com.task.mobiadmin.data.RouteData
import com.task.mobiadmin.data.TaskData
import com.task.mobiadmin.fragment.BaseFragment
import com.task.mobiadmin.retrofitManager.ResponseListener
import com.task.mobiadmin.service.DownloadService
import com.task.mobiadmin.utils.AppUtils
import com.task.mobiadmin.utils.Const
import com.task.mobiadmin.utils.Utils
import kotlinx.android.synthetic.main.fg_task_detail.*
import kotlinx.android.synthetic.main.inc_sound_rec.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import java.text.DecimalFormat
import java.util.*


/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class TaskDetailFragment : BaseFragment(), TaskRecorderAndPlayer.RecorderCallbacks {

    private var vieww: View? = null
    var taskData: TaskData? = null
    private var taskRecorder: TaskRecorderAndPlayer? = null
    lateinit var googleMap: GoogleMap
    private var approveCall: Call<JsonObject>? = null
    private var taskCall: Call<JsonObject>? = null
    private var taskId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null && arguments.containsKey("taskId")) {
            taskId = arguments.getString("taskId")
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbar(true, "Task Details")
        if (vieww != null) {
            val parent = vieww!!.parent as ViewGroup?
            parent?.removeView(vieww)
        }
        try {
            vieww = inflater!!.inflate(R.layout.fg_task_detail, null)
        } catch (e: InflateException) {
            e.printStackTrace()
        }
        return vieww
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
        coordinator.visibility = View.GONE
    }

    private fun loadData() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val currentUserType = RequestBody.create(MediaType.parse("text/plain"), Const.TYPE_COMPANY)
        val taskId = RequestBody.create(MediaType.parse("text/plain"), taskId)
        taskCall = apiInterface.singleTask(sessionId, currentUserType, taskId)
        apiManager.makeApiCall(taskCall, this)
    }

    private fun initUI() {
        coordinator.visibility = View.VISIBLE
        val params = appBar.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as AppBarLayout.Behavior?
        behavior?.onNestedFling(coordinator, appBar, appBar, 0f, 0f, true)

        tAddressTV.text = taskData!!.address
        AppUtils().loadStaticMap(baseActivity, tAddIV, taskData!!.latitude, taskData!!.longitude, true)
        val tg = AppUtils.MyTarget(userRIV, baseActivity, false)
        baseActivity.picasso.load(Const.ASSETS_BASE_URL + taskData!!.empData.photo).placeholder(R.mipmap.ic_default_user).into(tg)
        userRIV.tag = tg
        nameTV.text = taskData!!.empData.name
        phoneTV.text = taskData!!.empData.phone
        deadLineTV.text = "End Time : " + Utils.changeDateFormat(taskData!!.outTime, "yyyy-MM-dd HH:mm:ss", "dd MMM, hh:mma")
        titleTV.text = taskData!!.heading
        descTV.text = taskData!!.taskDescription
        if (taskData!!.unreadMsg > 0) {
            msgCountTV.visibility = View.VISIBLE
            msgCountTV.text = taskData!!.unreadMsg.toString()
        }
        if (!taskData!!.audioFile.isEmpty()) {
            audioLL.visibility = View.VISIBLE
            switchVisibility(false)
            audioLL.setOnClickListener(this)
        } else {
            audioLL.visibility = View.GONE
        }
        if (taskData!!.taskSignData != null) {
            loginCL.visibility = View.VISIBLE
            AppUtils().loadStaticMap(baseActivity, loginMapIV, taskData!!.taskSignData!!.signinLat, taskData!!.taskSignData!!.signinLong, true)
            val target = AppUtils.MyTarget(loginPicIV, baseActivity, true)
            baseActivity.picasso.load(Const.ASSETS_BASE_URL + taskData!!.taskSignData!!.signinpicture).placeholder(R.mipmap.ic_default_user).into(target)
            loginPicIV.tag = target
            loginAddressTV.text = taskData!!.taskSignData!!.signinAddress
            loginTimeTV.text = "SignIn Time: " + Utils.changeDateFormat(Utils.getCurrentTimeZoneTime(taskData!!.taskSignData!!.signindate, "yyyy-MM-dd HH:mm:ss")
                    , "yyyy-MM-dd HH:mm:ss", "dd MMM, hh:mma")
            if (taskData!!.status.toInt() >= Const.TASK_COMPLETE) {
                logoutCL.visibility = View.VISIBLE
                AppUtils().loadStaticMap(baseActivity, logoutMapIV, taskData!!.taskSignData!!.signoutLat, taskData!!.taskSignData!!.signoutLong, true)
                val tgt = AppUtils.MyTarget(logoutPicIV, baseActivity, true)
                baseActivity.picasso.load(Const.ASSETS_BASE_URL + taskData!!.taskSignData!!.signoutpicture).placeholder(R.mipmap.ic_default_user).into(tgt)
                logoutPicIV.tag = tgt
                logoutAddressTV.text = taskData!!.taskSignData!!.signoutAddress
                logoutTimeTV.text = "SignOut Time: " + Utils.changeDateFormat(Utils.getCurrentTimeZoneTime(taskData!!.taskSignData!!.signoutdate, "yyyy-MM-dd HH:mm:ss")
                        , "yyyy-MM-dd HH:mm:ss", "dd MMM, hh:mma")
                workdoneTV.text = taskData!!.taskSignData!!.workdone

                editTV.visibility = View.GONE
                if (taskData!!.status.toInt() == Const.TASK_COMPLETE) {
                    completeRL.visibility = View.VISIBLE
                }

                deadLineTV.text = "Task Duration : " + getTaskDuration()

                initializeMap()

                completeBT.setOnClickListener(this)
            }
        }
        callBT.setOnClickListener(this)
        editTV.setOnClickListener(this)
        expandIV.setOnClickListener(this)
        commentsBT.setOnClickListener(this)
        topCL.setOnClickListener(null)
    }

    private fun getTaskDuration(): String? {
        val startDate = Utils.getDateFromStringDate(taskData!!.taskSignData!!.signindate, "yyyy-MM-dd HH:mm:ss")
        val endDate = Utils.getDateFromStringDate(taskData!!.taskSignData!!.signoutdate, "yyyy-MM-dd HH:mm:ss")
        val diff = endDate.time - startDate.time
        log("" + diff)
        val hours = diff / (60 * 60 * 1000)
        val mins = (diff - (hours * 60 * 60 * 1000)) / (60 * 1000)
        return "" + hours + "h " + mins + "m"
    }

    private fun initializeMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            map.uiSettings.isMyLocationButtonEnabled = false
            map.uiSettings.setAllGesturesEnabled(false)
            map.uiSettings.isMapToolbarEnabled = false
            map.setOnMarkerClickListener { true }
            googleMap = map
            MapPathLoad(this, baseActivity, clickV, emptyTV, taskTimePB)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            taskRecorder?.onStop()
        } catch (ignored: Exception) {
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.audioLL ->
                if (!DownloadService.isRunning)
                    if (!taskData!!.downloadedFile.isEmpty()) playFile()
                    else {
                        val intent = Intent(baseActivity, DownloadService::class.java)
                        intent.putExtra("url", Const.ASSETS_BASE_URL + taskData!!.audioFile)
                        intent.putExtra("id", taskData!!.id)
                        baseActivity.startService(intent)
                        switchVisibility(true)
                        audioTV.text = "0%"
                    }
                else showToast("One downloading is already in progress")
            R.id.callBT -> {
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:" + taskData!!.empData.phone)
                val chooser = Intent.createChooser(callIntent, "Call via")
                baseActivity.startActivity(chooser)
            }
            R.id.editTV -> {
                val frag = TaskUpdateFragment()
                val bundle = Bundle()
                bundle.putParcelable("data", taskData!!)
                frag.arguments = bundle
                baseActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_container, frag)
                        .addToBackStack(null)
                        .commit()
            }
            R.id.expandIV ->
                if (descTV.maxLines == 5) {
                    descTV.maxLines = Int.MAX_VALUE
                    expandIV.rotation = 180.0f
                } else {
                    descTV.maxLines = 5
                    expandIV.rotation = 0.0f
                }
            R.id.completeBT -> showApproveDialog()
            R.id.commentsBT -> {
                msgCountTV.visibility = View.GONE
                val bndl = Bundle()
                bndl.putString("taskId", taskData!!.id)
                bndl.putString("toId", taskData!!.userid)
                if (taskData!!.status.toInt() == Const.TASK_APPROVED)
                    bndl.putBoolean("isApproved", true)
                val frag = TaskCommentsFragment()
                frag.arguments = bndl
                baseActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_container, frag)
                        .addToBackStack(null)
                        .commit()
            }
        }
    }

    private fun showApproveDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Task Approve")
        builder.setMessage("Are you sure you want to approve this task?")
        builder.setPositiveButton("Yes") { _, _ -> approveTask() }
        builder.setNegativeButton("No", null)
        builder.create().show()
    }

    private fun approveTask() {
        val sessId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val taskId = RequestBody.create(MediaType.parse("text/plain"), taskData!!.id)
        approveCall = apiInterface.approveTask(sessId, taskId)
        apiManager.makeApiCall(approveCall, this)
    }

    override fun onSuccess(call: Call<*>?, `object`: Any?) {
        super.onSuccess(call, `object`)
        if (approveCall != null && approveCall === call) {
            showToast("Task updated successfully")
            (baseActivity as MainActivity).gotoHomeFragment()
        } else if (taskCall != null && taskCall === call) {

            val jsonObject = `object` as JsonObject
            val jsonData = jsonObject.getAsJsonObject("data")
            val objectType = object : TypeToken<TaskData>() {}.type
            taskData = Gson().fromJson<TaskData>(jsonData, objectType)
            initUI()
        }
    }

    override fun onError(call: Call<*>?, statusCode: Int, errorMessage: String?, responseListener: ResponseListener?) {
        if (taskCall != null && taskCall == call) {
            showToast("Error loading details")
            (baseActivity as MainActivity).gotoHomeFragment()
        } else
            super.onError(call, statusCode, errorMessage, responseListener)
    }

    private fun playFile() {
        if (recordL.visibility == View.GONE) {
            recordL.visibility = View.VISIBLE
            val anim = AnimationUtils.loadAnimation(baseActivity, R.anim.slide_from_top)
            recordL.startAnimation(anim)
            taskRecorder = TaskRecorderAndPlayer(baseActivity, recordingTimeTV, firstCL, elapseTV,
                    totalTV, progressSB, recordPlayPauseIBT, stopIBT, closeIBT, this)
            try {
                taskRecorder?.createPlayerViewAndLoadFile(taskData!!.downloadedFile)
            } catch (e: Exception) {
                showToast("Invalid file", true)
                taskRecorder?.onStop()
            }
            baseActivity.hideSoftKeyboard()
        }
    }

    override fun onRecorderClose() {
        val anim = AnimationUtils.loadAnimation(baseActivity, R.anim.slide_out_top)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                recordL.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })
        recordL.startAnimation(anim)
        taskRecorder = null
    }

    private fun switchVisibility(isDownloading: Boolean) {
        audioIV.visibility = if (isDownloading) View.GONE else View.VISIBLE
        downloadPB.visibility = if (isDownloading) View.VISIBLE else View.GONE
        if (!isDownloading) audioTV.text = "Audio"
        audioLL.setOnClickListener(if (isDownloading) null else this)
    }

    fun updateProgress(id: Int, progress: Int) {
        if (isVisible && id == taskData!!.id.toInt())
            baseActivity.runOnUiThread {
                switchVisibility(true)
                audioTV.text = progress.toString() + "%"
            }
    }

    fun onCompleted(id: Int, path: String) {
        if (isVisible && id == taskData!!.id.toInt()) {
            taskData!!.downloadedFile = path
            log("Final path is " + taskData!!.downloadedFile)
            playFile()
            baseActivity.runOnUiThread { switchVisibility(false) }
        }
    }

    fun onFileFailed(id: Int) {
        if (isVisible && id == taskData!!.id.toInt()) baseActivity.runOnUiThread { switchVisibility(false) }
    }

    fun updateText(datas: ArrayList<RouteData>) {
        var totalDistance = 0.0
        for (i in 0..datas.size - 2) {
            val data = datas[i]
            val nextData = datas[i + 1]
            val floats = FloatArray(5)
            Location.distanceBetween(data.lat, data.lng, nextData.lat, nextData.lng, floats)
            if (floats[0].toDouble() != 0.0) totalDistance += floats[0] / 1000
        }
        val df = DecimalFormat("###.##")
        taskTimeTV.text = "" + df.format(totalDistance) + " Km"
    }
}