package com.task.mobi.fragment

import android.Manifest
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.task.mobi.R
import com.task.mobi.activity.MainActivity
import com.task.mobi.data.TaskData
import com.task.mobi.data.UserData
import com.task.mobi.retrofitManager.ProgressDialog
import com.task.mobi.service.DownloadService
import com.task.mobi.utils.*
import com.task.mobi.utils.maps.LocationManager
import com.task.mobi.utils.maps.MapUtils
import com.task.mobiadmin.fragment.home.TaskCommentsFragment
import com.task.mobiadmin.fragment.home.TaskRecorderAndPlayer
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fg_task_detail.*
import kotlinx.android.synthetic.main.inc_sound_rec.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class TaskDetailFragment : BaseFragment(), LocationManager.LocationUpdates, PermissionsManager.PermissionCallback
        , ImageUtils.ImageSelectCallback
        , TaskRecorderAndPlayer.RecorderCallbacks {

    lateinit var taskData: TaskData
    lateinit var taskId: String
    private var taskCall: Call<JsonObject>? = null
    lateinit var locationManager: LocationManager
    private var userLocation: Location? = null
    private var imageFile: File? = null
    private var signInCall: Call<JsonObject>? = null
    private var signOutCall: Call<JsonObject>? = null
    private var goBack: Boolean = false
    private var dialog: ProgressDialog? = null
    private var taskRecorder: TaskRecorderAndPlayer? = null

    internal var addresss = "No Address to this location"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null && arguments.containsKey("taskId")) {
            taskId = arguments.getString("taskId")
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater!!.inflate(R.layout.fg_task_detail, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
        parentNSV.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        if (goBack)
            baseActivity.onBackPressed()
    }

    private fun loadData() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val currentUserType = RequestBody.create(MediaType.parse("text/plain"), Const.TYPE_USER)
        val taskId = RequestBody.create(MediaType.parse("text/plain"), taskId)
        taskCall = apiInterface.singleTask(sessionId, currentUserType, taskId)
        apiManager.makeApiCall(taskCall, this)
    }

    private fun initUI() {
        parentNSV.visibility = View.VISIBLE
        setToolbar(true, "Task Detail")
        locationManager = LocationManager()
        locationManager.startLocationManager(baseActivity, LocationManager.Accuracy.HIGH, this)
        dialog = ProgressDialog.getInstance(baseActivity)
        dialog?.initiateProgressDialog()

        taskHeadTV.text = taskData.heading
        taskDescTV.text = taskData.taskDescription
        inTimeTV.text = getString(R.string.start_time, Utils.changeDateFormat(taskData.inTime, "yyyy-MM-dd HH:mm:ss", "dd MMM, hh:mm a"))
        outTimeTV.text = getString(R.string.end_time, Utils.changeDateFormat(taskData.outTime, "yyyy-MM-dd HH:mm:ss", "dd MMM, hh:mm a"))
        addressTV.text = "Task Location: " + taskData.address
        workDoneET.visibility = if (taskData.status == Const.TASK_NEW) View.GONE else View.VISIBLE
        if (taskData.status == Const.TASK_NEW) {
            submitBT.text = "Mark Sign In"
        } else {
            submitBT.text = "Mark Sign Out"
        }
        picIV.setOnClickListener(this)
        submitBT.setOnClickListener(this)
        locIV.setOnClickListener(this)
        directionsIV.setOnClickListener(this)
        if (!taskData.audioFile.isEmpty()) {
            audioLL.visibility = View.VISIBLE
            switchVisibility(false)
            audioLL.setOnClickListener(this)
        } else {
            audioLL.visibility = View.GONE
        }
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
            val frag = TaskCommentsFragment()
            frag.arguments = bndl
            baseActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_container, frag)
                    .addToBackStack(null)
                    .commit()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            taskRecorder?.onStop()
        } catch (ignored: Exception) {
        }
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.picIV -> if (PermissionsManager.checkPermissions(baseActivity, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 121, this))
                callCamera()
            R.id.submitBT -> if (validate()) updateTask()
            R.id.locIV -> getLocation()
            R.id.directionsIV -> showDirections()
            R.id.audioLL -> {
                if (!DownloadService.isRunning) {
                    if (taskData.downloadedFile != null && !taskData.downloadedFile.isEmpty()) {
                        playFile()
                    } else {
                        val intent = Intent(baseActivity, DownloadService::class.java)
                        intent.putExtra("url", Const.ASSETS_BASE_URL + taskData.audioFile)
                        intent.putExtra("id", taskData.id)
                        baseActivity.startService(intent)
                        switchVisibility(true)
                        audioTV.text = "0%"
                    }
                } else {
                    showToast("One downloading is already in progress")
                }
            }
        }
    }

    private fun playFile() {
        if (recordL.visibility == View.GONE) {
            recordL.visibility = View.VISIBLE
            val anim = AnimationUtils.loadAnimation(baseActivity, R.anim.slide_from_top)
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

    fun switchVisibility(isDownloading: Boolean) {
        audioIV.visibility = if (isDownloading) View.GONE else View.VISIBLE
        downloadPB.visibility = if (isDownloading) View.VISIBLE else View.GONE
        if (!isDownloading)
            audioTV.text = "Audio"
        audioLL.setOnClickListener(if (isDownloading) null else this)
    }

    private fun callCamera() {
        ImageUtils.ImageSelect.Builder(baseActivity, this, 120).onlyCamera(true).start()
    }

    fun getLocation() {
        if (userLocation == null) {
            locationManager.getLocation()
        } else {
            val gmmIntentUri = Uri.parse("geo:" + userLocation!!.latitude + "," + userLocation!!.longitude
                    + "?q=" + userLocation!!.latitude + "," + userLocation!!.longitude)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.`package` = "com.google.android.apps.maps"
            if (mapIntent.resolveActivity(baseActivity.packageManager) != null) {
                startActivity(mapIntent)
            } else {
                baseActivity.startActivity(Intent.createChooser(mapIntent, "Open Using"))
            }
        }
    }

    fun showDirections() {
        val gmmIntentUri = Uri.parse("google.navigation:q=" + taskData.latitude + "," + taskData.longitude)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.`package` = "com.google.android.apps.maps"
        if (mapIntent.resolveActivity(baseActivity.packageManager) != null) {
            startActivity(mapIntent)
        } else {
            baseActivity.startActivity(Intent.createChooser(mapIntent, "Open Using"))
        }
    }

    private fun updateTask() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val taskId = RequestBody.create(MediaType.parse("text/plain"), taskData.id)
        val lat = RequestBody.create(MediaType.parse("text/plain"), userLocation!!.latitude.toString())
        val lng = RequestBody.create(MediaType.parse("text/plain"), userLocation!!.longitude.toString())
        val address = RequestBody.create(MediaType.parse("text/plain"), getText(yourAddressTV))
        var image: MultipartBody.Part? = null
        try {
            image = MultipartBody.Part.createFormData("pic", imageFile?.name, RequestBody.create(MediaType.parse("image/jpeg"), imageFile!!))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (taskData.status == Const.TASK_NEW) {
            signInCall = apiInterface.signInTask(sessionId, taskId, lat, lng, address, image)
            apiManager.makeApiCall(signInCall, this)
        } else {
            val workdone = RequestBody.create(MediaType.parse("text/plain"), getText(workDoneET))
            signOutCall = apiInterface.signOutTask(sessionId, taskId, lat, lng, address, workdone, image)
            apiManager.makeApiCall(signOutCall, this)
        }
    }

    private fun validate(): Boolean {
        if (userLocation == null) {
            showToast("Unable to fetch location. Please check GPS is ON or not.")
        } else if (imageFile == null) {
            showToast("Please add image")
        } else if (taskData.status == Const.TASK_START && getText(workDoneET).isEmpty()) {
            showToast("Please enter workdone for task")
        } else
            return true
        return false
    }

    override fun onSuccess(call: Call<*>, `object`: Any) {
        super.onSuccess(call, `object`)
        if (taskCall != null && taskCall === call) {
            val jsonObject = `object` as JsonObject
            val jsonData = jsonObject.getAsJsonObject("data")
            val objectType = object : TypeToken<TaskData>() {}.type
            taskData = Gson().fromJson<TaskData>(jsonData, objectType)
            initUI()
        } else {
            showToast("Task Updated successfully")
            baseActivity.onBackPressed()
            if (signInCall != null && call === signInCall) {
                store.saveUserData(Const.LAST_LOCATION, null)
                Utils.startService(baseActivity, taskData.id, false)
            } else {
                Utils.stopService(baseActivity)
            }
        }
    }

    fun onLocationEnable() {
        locationManager.onLocationEnable()
    }

    fun onGPSEnableDenied() {
        showToast("Please check GPS is ON or not")
        goBack = true
    }

    override fun onStartingGettingLocation() {
        dialog?.stopProgressDialog()
    }

    @Synchronized override fun onLocationFound(location: Location) {
        try {
            userLocation = location
            dialog?.stopProgressDialog()
            AppUtils().loadStaticMap(baseActivity, locIV, location.latitude.toString(), location.longitude.toString(), true)
            Observable.just(MapUtils.with(baseActivity).getAddressFromLatLng(location.latitude, location.longitude))
                    .subscribe { address ->
                        log("After rx call single " + address)
                        if (address != "Not Found") {
                            yourAddressTV!!.text = address
                        } else {
                            log("Starting rx call Address")
                            val url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + userLocation!!.latitude + "," + userLocation!!.longitude + "&sensor=true"
                            val call = apiInterface.getDecodeAddress(url)
                            call.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnComplete {
                                        log("After rx call Address")
                                        yourAddressTV.text = addresss
                                    }
                                    .subscribe({ jsonObject ->
                                        if (jsonObject.getAsJsonArray("results").size() > 0)
                                            addresss = jsonObject.getAsJsonArray("results").get(0).asJsonObject.get("formatted_address").asString
                                    }, { error: Throwable? ->
                                        log("After rx call Error")
                                        yourAddressTV.text = addresss
                                    })
                        }
                    }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onLocationNotFound() {
        showToast("Unable to find your current location.")
    }

    override fun onLocationPermissionDenied() {
        goBack = true
    }

    override fun onPermissionsGranted(resultCode: Int) {
        if (resultCode == 121) {
            callCamera()
        }
    }

    override fun onPermissionsDenied(resultCode: Int) {

    }

    override fun onImageSelected(imagePath: String, resultCode: Int) {
        val bitmap = ImageUtils.imageCompress(imagePath)
        picIV!!.setImageBitmap(bitmap)
        imageFile = ImageUtils.bitmapToFile(bitmap, baseActivity)
        log("Size >>>>>>> " + imageFile!!.length())
    }

    fun updateProgress(id: Int, progress: Int) {
        if (isVisible && id == taskData.id.toInt()) {
            baseActivity.runOnUiThread({
                switchVisibility(true)
                audioTV.text = progress.toString() + "%"
            })
        }
    }

    fun onCompleted(id: Int, path: String) {
        if (isVisible && id == taskData.id.toInt()) {
            taskData.downloadedFile = path
            log("Final path is " + taskData.downloadedFile)
            playFile()
            baseActivity.runOnUiThread({
                switchVisibility(false)
            })
        }
    }

    fun onFileFailed(id: Int) {
        if (isVisible && id == taskData.id.toInt()) {
            baseActivity.runOnUiThread({
                switchVisibility(false)
            })
        }
    }
}