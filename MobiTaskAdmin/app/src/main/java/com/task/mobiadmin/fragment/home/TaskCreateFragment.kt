package com.task.mobiadmin.fragment.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.task.mobiadmin.R
import com.task.mobiadmin.activity.MainActivity
import com.task.mobiadmin.data.EmpData
import com.task.mobiadmin.fragment.AddLocationPinFragment
import com.task.mobiadmin.fragment.BaseFragment
import com.task.mobiadmin.fragment.PinAdd
import com.task.mobiadmin.fragment.empMngmnt.EmpCreateFragment
import com.task.mobiadmin.retrofitManager.ResponseListener
import com.task.mobiadmin.utils.AppUtils
import com.task.mobiadmin.utils.Const
import kotlinx.android.synthetic.main.fg_task_create.*
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
class TaskCreateFragment : BaseFragment(), TaskRecorderAndPlayer.RecorderCallbacks, PinAdd.AddPinListener {


    private lateinit var pinAdd: PinAdd
    private var taskAddress: String? = null
    private var taskLoc: LatLng? = null
    private var empCall: Call<JsonObject>? = null
    private var createCall: Call<JsonObject>? = null
    var datas = ArrayList<EmpData>()
    private var currentCal: Calendar = Calendar.getInstance()
    private var startCal: Calendar = Calendar.getInstance()
    private var endCal: Calendar = Calendar.getInstance()

    private var vieww: View? = null

    private var isLoaded: Boolean = false
    private var userData: EmpData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null && arguments.containsKey("data")) {
            userData = arguments.getParcelable("data")
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbar(true, "Create Task")
        if (vieww == null) {
            isLoaded = true
            vieww = inflater!!.inflate(R.layout.fg_task_create, null)
        }
        return vieww
    }

    private var taskRecorder: TaskRecorderAndPlayer? = null

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
        TransitionManager.beginDelayedTransition(taskCL)
        addVoiceTV.visibility = View.VISIBLE
        recordL.visibility = View.GONE
        taskRecorder = null
    }

    private fun initUI() {
        addVoiceTV.setOnClickListener {
            if (recordL.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(taskCL)
                addVoiceTV.visibility = View.GONE
                recordL.visibility = View.VISIBLE
//                val anim = AnimationUtils.loadAnimation(baseActivity, R.anim.slide_from_top)
//                recordL.startAnimation(anim)

                taskRecorder = TaskRecorderAndPlayer(baseActivity, recordingTimeTV, firstCL, elapseTV,
                        totalTV, progressSB, recordPlayPauseIBT, stopIBT, closeIBT, this)
                baseActivity.hideSoftKeyboard()
            }
        }
        locationTV.setOnClickListener {
            pinAdd = PinAdd.instance
            pinAdd.setListener(this)
            baseActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_container, AddLocationPinFragment())
                    .addToBackStack(null)
                    .commit()
        }
        inTimeTV.setOnClickListener { showStartDateDialog() }
        outTimeTV.setOnClickListener { showEndDateDialog() }
        submitBT.setOnClickListener { if (validate()) createTask() }
        getUserList()
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

    private fun createTask() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val userId = RequestBody.create(MediaType.parse("text/plain"), userData?.id.toString())
        val heading = RequestBody.create(MediaType.parse("text/plain"), getText(titleET))
        val desc = RequestBody.create(MediaType.parse("text/plain"), getText(descET))
        val inTime = RequestBody.create(MediaType.parse("text/plain"), SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(startCal.time))
        val outTime = RequestBody.create(MediaType.parse("text/plain"), SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(endCal.time))
        val lat = RequestBody.create(MediaType.parse("text/plain"), taskLoc?.latitude.toString())
        val lng = RequestBody.create(MediaType.parse("text/plain"), taskLoc?.longitude.toString())
        val address = RequestBody.create(MediaType.parse("text/plain"), taskAddress)
        val isApprovable = RequestBody.create(MediaType.parse("text/plain"), if (approveCB.isChecked) "1" else "0")
        var audio: MultipartBody.Part? = null
        try {
            audio = MultipartBody.Part.createFormData("upload", taskRecorder?.getFile()?.name, RequestBody.create(MediaType.parse("audio/3gpp"), taskRecorder?.getFile()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        createCall = apiInterface.createTask(sessionId, userId, heading, desc, inTime, outTime, lat, lng, address, isApprovable, audio)
        apiManager.makeApiCall(createCall, this)
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

            //Set Spinner
            val list = arrayListOf<String>()
            for (each in datas)
                list.add(each.name)

            val dataAdapter = ArrayAdapter<String>(baseActivity, R.layout.adapter_emp_name_list, list)
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            nameSP.adapter = dataAdapter
            nameSP.onItemSelectedListener = this
            if (userData?.id != 0) {
                var i = 0
                for (d in datas) {
                    if (d.id == userData?.id) {
                        nameSP.setSelection(i)
                        nameSP.isEnabled = false
                        break
                    }
                    i++
                }
            } else
                loadUserData(datas[0])
        } else if (createCall != null && createCall === call) {
            showToast("Task created successfully")
            (baseActivity as MainActivity).gotoHomeFragment()
        }
    }

    private fun loadUserData(userData: EmpData) {
        this.userData = userData
        val target = AppUtils.MyTarget(userRIV, baseActivity, false)
        baseActivity.picasso.load(Const.ASSETS_BASE_URL + userData.photo).placeholder(R.mipmap.ic_default_user).into(target)
        userRIV.tag = target
        phoneTV.text = userData.phone
    }

    override fun onError(call: Call<*>?, statusCode: Int, errorMessage: String?, responseListener: ResponseListener?) {
        if (statusCode == Const.ErrorCodes.NO_USER) showDialogUserCreate()
        else super.onError(call, statusCode, errorMessage, responseListener)
    }

    private fun showDialogUserCreate() {
        val bldr = AlertDialog.Builder(baseActivity)
        bldr.setTitle("Create User")
        bldr.setMessage("You need to create a user first")
        bldr.setCancelable(false)
        bldr.setPositiveButton("Create") { _, _ ->
            baseActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_container, EmpCreateFragment())
                    .commit()
        }
        bldr.setNegativeButton("Cancel") { _, _ -> baseActivity.supportFragmentManager.popBackStack() }
        bldr.create().show()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        super.onItemSelected(parent, view, position, id)
        val userData = datas[position]
        loadUserData(userData)
    }
}