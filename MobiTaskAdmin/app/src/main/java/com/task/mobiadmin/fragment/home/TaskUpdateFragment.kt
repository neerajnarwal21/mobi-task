package com.task.mobiadmin.fragment.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.task.mobiadmin.R
import com.task.mobiadmin.activity.MainActivity
import com.task.mobiadmin.data.EmpData
import com.task.mobiadmin.data.TaskData
import com.task.mobiadmin.fragment.AddLocationPinFragment
import com.task.mobiadmin.fragment.BaseFragment
import com.task.mobiadmin.fragment.PinAdd
import com.task.mobiadmin.service.DownloadService
import com.task.mobiadmin.utils.AppUtils
import com.task.mobiadmin.utils.Const
import com.task.mobiadmin.utils.Utils
import kotlinx.android.synthetic.main.fg_task_update.*
import kotlinx.android.synthetic.main.inc_sound_rec.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class TaskUpdateFragment : BaseFragment(), TaskRecorderAndPlayer.RecorderCallbacks, PinAdd.AddPinListener {


    private lateinit var pinAdd: PinAdd
    private var taskAddress: String? = null
    private var taskLoc: LatLng? = null
    private var empCall: Call<JsonObject>? = null
    private var updateCall: Call<JsonObject>? = null
    private var deleteCall: Call<JsonObject>? = null
    var datas = ArrayList<EmpData>()
    private var currentCal: Calendar = Calendar.getInstance()
    private var startCal: Calendar = Calendar.getInstance()
    private var endCal: Calendar = Calendar.getInstance()

    private var vieww: View? = null

    private var isLoaded: Boolean = false
    private lateinit var userData: EmpData
    private lateinit var taskData: TaskData

    private var taskRecorder: TaskRecorderAndPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null && arguments.containsKey("data")) {
            taskData = arguments.getParcelable("data")
            userData = taskData.empData
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbar(true, "Update Task")
        pinAdd = PinAdd.instance
        if (vieww == null) {
            isLoaded = true
            vieww = inflater!!.inflate(R.layout.fg_task_update, null)
        }
        return vieww
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isLoaded) {
            isLoaded = false
            baseActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            initUI()
            parentCL.visibility = View.INVISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            taskRecorder?.onStop()
        } catch (ignored: Exception) {
        }
    }

    override fun onResume() {
        super.onResume()
        if (taskAddress != null) locationTV.text = taskAddress
    }

    override fun onRecorderClose() {
        val anim = AnimationUtils.loadAnimation(baseActivity, R.anim.slide_out_top_center)
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

    private fun initUI() {
        addVoiceTV.setOnClickListener(this)
        locationTV.setOnClickListener(this)
        inTimeTV.setOnClickListener(this)
        outTimeTV.setOnClickListener(this)
        submitBT.setOnClickListener(this)
        deleteBT.setOnClickListener(this)
        if (!taskData.audioFile.isEmpty()) {
            audioLL.visibility = View.VISIBLE
            switchVisibility(false)
            audioLL.setOnClickListener(this)
        } else audioLL.visibility = View.GONE
        if (taskData.status.toInt() == Const.TASK_START) deleteBT.visibility = View.GONE
        getUserList()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.addVoiceTV ->
                if (recordL.visibility == View.GONE) {
                    recordL.visibility = View.VISIBLE
                    val anim = AnimationUtils.loadAnimation(baseActivity, R.anim.slide_from_top)
                    recordL.startAnimation(anim)
                    taskRecorder = TaskRecorderAndPlayer(baseActivity, recordingTimeTV, firstCL, elapseTV,
                            totalTV, progressSB, recordPlayPauseIBT, stopIBT, closeIBT, this)
                    baseActivity.hideSoftKeyboard()
                }
            R.id.audioLL ->
                if (!DownloadService.isRunning)
                    if (!taskData.downloadedFile.isEmpty()) playFile()
                    else {
                        val intent = Intent(baseActivity, DownloadService::class.java)
                        intent.putExtra("url", Const.ASSETS_BASE_URL + taskData.audioFile)
                        intent.putExtra("id", taskData.id)
                        baseActivity.startService(intent)
                        switchVisibility(true)
                        audioTV.text = "0%"
                    }
                else showToast("One downloading is already in progress")
            R.id.locationTV -> {
                pinAdd.setListener(this)
                baseActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_container, AddLocationPinFragment())
                        .addToBackStack(null)
                        .commit()
            }
            R.id.inTimeTV -> showStartDateDialog()
            R.id.outTimeTV -> showEndDateDialog()
            R.id.submitBT -> if (validate()) updateTask()
            R.id.deleteBT -> showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        val bldr = AlertDialog.Builder(baseActivity)
        bldr.setTitle("Delete Task")
        bldr.setMessage("Are you sure you want to delete this task ?")
        bldr.setPositiveButton("Yes") { _, _ -> deleteTask() }
        bldr.setNegativeButton("No", null)
        bldr.create().show()
    }

    private fun deleteTask() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val taskId = RequestBody.create(MediaType.parse("text/plain"), taskData.id)
        val userId = RequestBody.create(MediaType.parse("text/plain"), taskData.empData.id.toString())
        deleteCall = apiInterface.deleteTask(sessionId, taskId, userId)
        apiManager.makeApiCall(deleteCall, this)
    }

    private fun playFile() {
        if (recordL.visibility == View.GONE) {
            recordL.visibility = View.VISIBLE
            val anim = AnimationUtils.loadAnimation(baseActivity, R.anim.slide_from_top_left)
            recordL.startAnimation(anim)
            taskRecorder = TaskRecorderAndPlayer(baseActivity, recordingTimeTV, firstCL, elapseTV,
                    totalTV, progressSB, recordPlayPauseIBT, stopIBT, closeIBT, this)
            try {
                taskRecorder?.createPlayerViewAndLoadFile(taskData.downloadedFile)
            } catch (e: Exception) {
                showToast("Invalid file", true)
                taskRecorder?.onStop()
            }
            baseActivity.hideSoftKeyboard()
        }
    }

    private fun switchVisibility(isDownloading: Boolean) {
        audioIV.visibility = if (isDownloading) View.GONE else View.VISIBLE
        downloadPB.visibility = if (isDownloading) View.VISIBLE else View.GONE
        if (!isDownloading) audioTV.text = "Audio"
        audioLL.setOnClickListener(if (isDownloading) null else this)
    }

    fun updateProgress(id: Int, progress: Int) {
        if (isVisible && id == taskData.id.toInt()) {
            baseActivity.runOnUiThread {
                switchVisibility(true)
                audioTV.text = progress.toString() + "%"
            }
        }
    }

    fun onCompleted(id: Int, path: String) {
        if (isVisible && id == taskData.id.toInt()) {
            taskData.downloadedFile = path
            log("Final path is " + taskData.downloadedFile)
            playFile()
            baseActivity.runOnUiThread { switchVisibility(false) }
        }
    }

    fun onFileFailed(id: Int) {
        if (isVisible && id == taskData.id.toInt()) baseActivity.runOnUiThread { switchVisibility(false) }
    }

    private fun showStartDateDialog() {
        val datePickerDialog = DatePickerDialog(baseActivity,
                DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    startCal = Calendar.getInstance()
                    startCal.set(year, monthOfYear, dayOfMonth)
                    if (startCal.before(currentCal)) {
                        showToast("Please select a future date")
                    } else {
                        val timePickerDialog = TimePickerDialog(baseActivity, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                            startCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            startCal.set(Calendar.MINUTE, minute)
                            if (startCal.before(currentCal)) {
                                showToast("Please select a future time")
                            } else {
                                inTimeTV.text = SimpleDateFormat("MMM dd, hh:mma", Locale.ENGLISH).format(startCal.time)
                                outTimeTV.text = ""
                            }
                        }, currentCal.get(Calendar.HOUR_OF_DAY), currentCal.get(Calendar.MINUTE), false)
                        timePickerDialog.show()
                    }
                }, currentCal.get(Calendar.YEAR), currentCal.get(Calendar.MONTH), currentCal.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    private fun showEndDateDialog() {
        if (getText(inTimeTV).isEmpty()) {
            showToast("Please select Start date first")
        } else {
            val datePickerDialog = DatePickerDialog(baseActivity,
                    DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                        endCal = Calendar.getInstance()
                        endCal.set(year, monthOfYear, dayOfMonth)
                        if (endCal.get(Calendar.DAY_OF_YEAR) < startCal.get(Calendar.DAY_OF_YEAR)) {
                            showToast("Please select a future date than start date")
                        } else {
                            val timePickerDialog = TimePickerDialog(baseActivity, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                                endCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                endCal.set(Calendar.MINUTE, minute)
                                if (endCal.before(startCal)) {
                                    showToast("Please select a future time than start time")
                                } else {
                                    outTimeTV.text = SimpleDateFormat("MMM dd, hh:mma", Locale.ENGLISH).format(endCal.time)
                                }
                            }, currentCal.get(Calendar.HOUR_OF_DAY), currentCal.get(Calendar.MINUTE), false)
                            timePickerDialog.show()
                        }
                    }, currentCal.get(Calendar.YEAR), currentCal.get(Calendar.MONTH), currentCal.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }
    }

    private fun updateTask() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val taskId = RequestBody.create(MediaType.parse("text/plain"), taskData.id)
        val userId = RequestBody.create(MediaType.parse("text/plain"), userData.id.toString())
        val heading = RequestBody.create(MediaType.parse("text/plain"), getText(titleET))
        val desc = RequestBody.create(MediaType.parse("text/plain"), getText(descET))
        val inTime = RequestBody.create(MediaType.parse("text/plain"), SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(startCal.time))
        val outTime = RequestBody.create(MediaType.parse("text/plain"), SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(endCal.time))
        val lat = RequestBody.create(MediaType.parse("text/plain"), taskLoc?.latitude.toString())
        val lng = RequestBody.create(MediaType.parse("text/plain"), taskLoc?.longitude.toString())
        val address = RequestBody.create(MediaType.parse("text/plain"), taskAddress)
        val isApprovable = RequestBody.create(MediaType.parse("text/plain"), if (approveCB.isChecked) "1" else "0")
        val taskStatus = RequestBody.create(MediaType.parse("text/plain"), taskData.status)
        var audio: MultipartBody.Part? = null
        try {
            audio = MultipartBody.Part.createFormData("upload", taskRecorder?.getFile()?.name, RequestBody.create(MediaType.parse("audio/3gpp"), taskRecorder?.getFile()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updateCall = apiInterface.updateTask(sessionId, taskId, userId, heading, desc, inTime, outTime, lat, lng, address, isApprovable, taskStatus, audio)
        apiManager.makeApiCall(updateCall, this)
    }

    private fun validate(): Boolean {
        when {
            getText(titleET).isEmpty() -> showToast("Please enter task title")
            getText(descET).isEmpty() -> showToast("Please enter task description")
            taskRecorder != null && taskRecorder?.playerMode == taskRecorder?.RECORDING ->
                showToast("Please stop the recording first")
            getText(inTimeTV).isEmpty() -> showToast("Please enter task Start Time")
            getText(outTimeTV).isEmpty() -> showToast("Please enter task End Time")
            taskAddress == null -> showToast("Please enter task location")
            else -> return true
        }
        return false
    }

    private fun getUserList() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        empCall = apiInterface.listUsers(sessionId)
        apiManager.makeApiCall(empCall, this)
    }

    override fun onLocationPinAdded(address: String, latLng: LatLng) {
        taskAddress = address
        taskLoc = latLng
    }

    override fun onSuccess(call: Call<*>?, `object`: Any?) {
        super.onSuccess(call, `object`)
        if (empCall === call) {
            parentCL.visibility = View.VISIBLE
            val jsonObject = `object` as JsonObject
            val data = jsonObject.getAsJsonArray("data")
            val objectType = object : TypeToken<ArrayList<EmpData>>() {}.type
            datas = Gson().fromJson<ArrayList<EmpData>>(data, objectType)

            setUI()
            //Set Spinner
            val list = arrayListOf<String>()
            for (each in datas)
                list.add(each.name)

            val dataAdapter = ArrayAdapter<String>(baseActivity, R.layout.adapter_emp_name_list, list)
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            nameSP.adapter = dataAdapter
            nameSP.onItemSelectedListener = this
            var i = 0
            for (d in datas) {
                if (d.id == userData.id) {
                    nameSP.setSelection(i)
                    break
                }
                i++
            }
        } else if (updateCall != null && updateCall === call) {
            showToast("Task updated successfully")
            (baseActivity as MainActivity).gotoHomeFragment()
        } else if (deleteCall != null && deleteCall === call) {
            showToast("Task deleted successfully")
            (baseActivity as MainActivity).gotoHomeFragment()
        }
    }

    private fun setUI() {
        titleET.setText(taskData.heading)
        descET.setText(taskData.taskDescription)
        startCal.time = Utils.getDateFromStringDate(taskData.inTime, "yyyy-MM-dd HH:mm:ss")
        endCal.time = Utils.getDateFromStringDate(taskData.outTime, "yyyy-MM-dd HH:mm:ss")
        inTimeTV.text = SimpleDateFormat("MMM dd, hh:mma", Locale.ENGLISH).format(startCal.time)
        outTimeTV.text = SimpleDateFormat("MMM dd, hh:mma", Locale.ENGLISH).format(endCal.time)
        taskLoc = LatLng(taskData.latitude.toDouble(), taskData.longitude.toDouble())
        taskAddress = taskData.address
        locationTV.text = taskAddress
        approveCB.isChecked = taskData.isApprovable.toInt() == 1
    }

    private fun loadUserData(userData: EmpData) {
        this.userData = userData
        val target = AppUtils.MyTarget(userRIV, baseActivity, false)
        baseActivity.picasso.load(Const.ASSETS_BASE_URL + userData.photo).placeholder(R.mipmap.ic_default_user).into(target)
        userRIV.tag = target
        phoneTV.text = userData.phone
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        super.onItemSelected(parent, view, position, id)
        val userData = datas[position]
        loadUserData(userData)
    }
}