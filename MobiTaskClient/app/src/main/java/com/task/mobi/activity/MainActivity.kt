package com.task.mobi.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.widget.DrawerLayout
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.JsonObject
import com.task.mobi.R
import com.task.mobi.data.UserData
import com.task.mobi.fragment.HomeFragment
import com.task.mobi.fragment.ProfileFragment
import com.task.mobi.fragment.TaskDetailFragment
import com.task.mobi.fragment.TaskHistoryFragment
import com.task.mobi.utils.AppUtils
import com.task.mobi.utils.Const
import com.task.mobi.utils.PermissionsManager
import com.task.mobi.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_custom.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call

class MainActivity : BaseActivity(), PermissionsManager.PermissionCallback {

    lateinit var logoutCall: Call<JsonObject>
    lateinit var drawer: DrawerLayout
    lateinit var commentsL: FrameLayout
    lateinit var messageCountsTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
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
        }
    }

    private fun gotoHomeFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_container, HomeFragment())
                .commit()
    }


    private fun initUI() {
        commentsL = commentsFL
        messageCountsTV = messageCountTV
        setSupportActionBar(toolbar)
        navIV.setOnClickListener { onBackPressed() }

        navigationView.itemIconTintList = null
        navigationView.setNavigationItemSelectedListener({ menuItem ->
            // This method will trigger on item Click of navigation menu
            menuItem.isChecked = true
            drawerLayout.closeDrawers()
            var fragment: Fragment? = null
            when (menuItem.itemId) {
                R.id.home -> fragment = HomeFragment()
                R.id.history -> fragment = TaskHistoryFragment()
//                R.id.about -> fragment = AboutUsFragment()
//                R.id.about -> fragment = TestFragment()

                R.id.logout -> showLogoutDialog()
//                R.id.exit -> finish()
            }

            if (fragment != null)
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_container, fragment)
                        .commit()
            true
        })
        drawer = drawerLayout
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        setUserData()
        PermissionsManager.checkPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 121, this)
    }

    private fun logoutUser() {
        logoutCall = apiInterface.logout(RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID)))
        apiManager.makeApiCall(logoutCall, this)
    }

    private fun showLogoutDialog() {
        val bldr = AlertDialog.Builder(this)
        bldr.setMessage("Are you sure you want to logout ?")
        bldr.setPositiveButton("Logout", { dialog, which -> logoutUser() })
        bldr.setNegativeButton("Cancel", null)
        bldr.create().show()
    }

    override fun onPermissionsGranted(resultCode: Int) {

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancelAll()
    }

    override fun onPermissionsDenied(resultCode: Int) {

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancelAll()
    }

    fun setUserData() {
        val view = navigationView.getHeaderView(0)
        val userIV = view.findViewById<ImageView>(R.id.userIV)
        val nameTV = view.findViewById<TextView>(R.id.nameTV)
        val userData = store.getUserData(Const.USER_DATA, UserData::class.java) as UserData
        val target = AppUtils.MyTarget(userIV)
        picasso.load(Const.ASSETS_BASE_URL + userData.photo).error(R.mipmap.ic_default_user).placeholder(R.mipmap.ic_default_user).into(target)
        userIV.tag = target
        nameTV.text = "Hi, " + userData.name
        view.setOnClickListener({
            drawerLayout.closeDrawers()
            supportFragmentManager.beginTransaction().replace(R.id.frame_container, ProfileFragment()).commit()
        })
    }

    fun setToolbar(show: Boolean, title: String) {
        commentsL.visibility = View.GONE
        messageCountsTV.visibility = View.GONE
        toolbar.visibility = if (show) View.VISIBLE else View.GONE
        viewV.visibility = /*if (show) View.VISIBLE else */View.GONE
        navIV.visibility = /*if (showBackIcon) View.VISIBLE else */View.GONE
        supportActionBar?.title = title
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        hideSoftKeyboard()
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawers()
        } else {
            val fragment = supportFragmentManager.findFragmentById(R.id.frame_container)
            if (fragment is HomeFragment) {
                showExit()
            } else if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                gotoHomeFragment()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = supportFragmentManager.findFragmentById(R.id.frame_container)

        if (fragment is TaskDetailFragment) {
            when (requestCode) {
                Const.REQUEST_LOCATION ->
                    when (resultCode) {
                        Activity.RESULT_OK -> fragment.onLocationEnable()
                        Activity.RESULT_CANCELED -> fragment.onGPSEnableDenied()
                        else -> fragment.onGPSEnableDenied()
                    }
            }
        }
    }

    override fun onSuccess(call: Call<*>, `object`: Any) {
        super.onSuccess(call, `object`)
        if (logoutCall === call) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
            Utils.logoutUser(this, store)
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
            }
        }
    }
}
