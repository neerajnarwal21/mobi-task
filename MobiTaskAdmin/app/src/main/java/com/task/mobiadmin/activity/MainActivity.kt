package com.task.mobiadmin.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.FragmentManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.gson.JsonObject
import com.task.mobiadmin.R
import com.task.mobiadmin.data.UserData
import com.task.mobiadmin.fragment.AddLocationPinFragment
import com.task.mobiadmin.fragment.ProfileFragment
import com.task.mobiadmin.fragment.TaskHistoryFragment
import com.task.mobiadmin.fragment.empMngmnt.EmpListFragment
import com.task.mobiadmin.fragment.home.HomeFragment
import com.task.mobiadmin.fragment.home.TaskDetailFragment
import com.task.mobiadmin.fragment.home.TaskUpdateFragment
import com.task.mobiadmin.utils.AppUtils
import com.task.mobiadmin.utils.Const
import com.task.mobiadmin.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call

class MainActivity : BaseActivity() {

    lateinit var logoutCall: Call<JsonObject>
    //    var profileCall: Call<JsonObject>? = null
    lateinit var toolBar: Toolbar
    var newTasks = 0
    var ongoingTasks = 0
    var completedTasks = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolBar = toolbar
        initUI()
//        getProfile()
        gotoHomeFragment()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    fun handleIntent(intent: Intent) {

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(1)
        if (intent.getBooleanExtra("fromPush", false)) {
            val action = intent.getStringExtra("action")
            when (action) {
                "new-chat" -> {
                    val toId = intent.getStringExtra("toId")
                    val taskId = intent.getStringExtra("taskId")

                    val bndl = Bundle()
                    bndl.putString("taskId", taskId)
                    bndl.putString("toId", toId)
                    val frag = TaskDetailFragment()
                    frag.arguments = bndl
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.frame_container, frag)
                            .commit()
                }
            }
        } else
            gotoHomeFragment()
    }

//    private fun getProfile() {
//        profileCall = apiInterface.getProfile(RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID)))
//        apiManager.makeApiCall(profileCall, false, this)
//    }

    fun getTotalTasksCount() = (newTasks + ongoingTasks + completedTasks).toString()

    fun gotoHomeFragment() {
        supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_container, HomeFragment())
//                .addToBackStack(null)
                .commit()
    }


    private fun initUI() {
        setSupportActionBar(toolbar)
        navigationView.itemIconTintList = null
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawer.closeDrawers()
            var fragment: Fragment? = null
            when (menuItem.itemId) {
                R.id.home -> fragment = HomeFragment()
                R.id.history -> fragment = TaskHistoryFragment()
                R.id.empList -> fragment = EmpListFragment()
                R.id.logout -> showLogoutDialog()
            }

            if (fragment != null)
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_container, fragment)
//                        .addToBackStack(null)
                        .commit()
            true
        }
        setUserData()
    }

    private fun showLogoutDialog() {
        val bldr = AlertDialog.Builder(this)
        bldr.setMessage("Are you sure you want to logout ?")
        bldr.setPositiveButton("Logout") { _, _ -> logoutUser() }
        bldr.setNegativeButton("Cancel", null)
        bldr.create().show()
    }

    private fun logoutUser() {
        logoutCall = apiInterface.logout(RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID)))
        apiManager.makeApiCall(logoutCall, this)
        Utils.logoutUser(this, store)
    }

    fun setUserData() {
        val view = navigationView.getHeaderView(0)
        val userIV = view.findViewById<ImageView>(R.id.userIV)
        val nameTV = view.findViewById<TextView>(R.id.nameTV)
        val userData = store.getUserData(Const.USER_DATA, UserData::class.java) as UserData
        val target = AppUtils.MyTarget(userIV, this, false)
        picasso.load(Const.ASSETS_BASE_URL + userData.photo).placeholder(R.mipmap.ic_default_company).error(R.mipmap.ic_default_company).into(target)
        userIV.tag = target
        nameTV.text = userData.companyname
        view.setOnClickListener {
            drawer.closeDrawers()
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_container, ProfileFragment())
//                    .addToBackStack(null)
                    .commit()
        }
    }

    fun setToolbar(show: Boolean, title: String) {
        toolbar.visibility = if (show) View.VISIBLE else View.GONE
        toolbar.title = title
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onBackPressed() {
        hideSoftKeyboard()
        if (drawer.isDrawerOpen(Gravity.START)) {
            drawer.closeDrawers()
        } else {
            val fragment = supportFragmentManager.findFragmentById(R.id.frame_container)
            if (fragment is HomeFragment) {
                showExit()
            } else if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else
                gotoHomeFragment()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = supportFragmentManager.findFragmentById(R.id.frame_container)
        if (fragment is AddLocationPinFragment) {
            fragment.onSearchComplete()
            when (requestCode) {
                Const.PLACE_REQUEST ->
                    when (resultCode) {
                        Activity.RESULT_OK -> {
                            val place = PlaceAutocomplete.getPlace(this, data)
                            fragment.onPlaceSelected(place)
                        }
                        PlaceAutocomplete.RESULT_ERROR -> {
                            val status = PlaceAutocomplete.getStatus(this, data)
                            showToast(status.statusMessage, true)
                        }
                    }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(downloadCompletion, IntentFilter("FileDownloaded"))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadCompletion)
    }
//
//    override fun onSuccess(call: Call<*>?, `object`: Any?) {
//        super.onSuccess(call, `object`)
//        if (profileCall != null && profileCall === call) {
//            val jsonObject = `object` as JsonObject
//            val data = jsonObject.getAsJsonObject("data")
//            val objectType = object : TypeToken<UserData>() {
//            }.type
//            val userData = Gson().fromJson<UserData>(data, objectType)
//            store.saveUserData(Const.USER_DATA, userData)
//            setUserData()
//        }
//    }


    var downloadCompletion: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val fragment = supportFragmentManager.findFragmentById(R.id.frame_container)
            if (fragment is TaskDetailFragment) {
                if (intent.getBooleanExtra("fail", false)) {
                    showToast("File is no longer available on server", true)
                    fragment.onFileFailed(intent.getIntExtra("id", 0))
                } else if (intent.hasExtra("progress")) {
                    fragment.updateProgress(intent.getIntExtra("id", 0), intent.getIntExtra("progress", 0))
                } else {
                    fragment.onCompleted(intent.getIntExtra("id", 0), intent.getStringExtra("path"))
                }
            } else if (fragment is TaskUpdateFragment) {
                if (intent.getBooleanExtra("fail", false)) {
                    showToast("File is no longer available on server", true)
                    fragment.onFileFailed(intent.getIntExtra("id", 0))
                } else if (intent.hasExtra("progress")) {
                    fragment.updateProgress(intent.getIntExtra("id", 0), intent.getIntExtra("progress", 0))
                } else {
                    fragment.onCompleted(intent.getIntExtra("id", 0), intent.getStringExtra("path"))
                }
            }
        }
    }
}
