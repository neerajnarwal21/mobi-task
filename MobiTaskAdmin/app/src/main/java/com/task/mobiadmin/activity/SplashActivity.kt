package com.task.mobiadmin.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import com.task.mobiadmin.R
import com.task.mobiadmin.utils.Const
import com.task.mobiadmin.utils.TokenRefresh
import com.task.mobiadmin.utils.Utils

/**
 * Created by Neeraj Narwal on 5/5/17.
 */

class SplashActivity : BaseActivity(), TokenRefresh.TokenListener {

    private val handler = Handler()
    private val waitingRunnable = Runnable {
        if (!isFinishing && store.getString(Const.DEVICE_TOKEN) == null) {
            showDialogNoServices()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        createNotificationChannel()
        log("" + Utils.getUniqueDeviceId(this))
        log("Token >>>> Start app")
        if (intent.hasExtra("isUpdate")) showUpdateDialog()
        else initApp()

        val mNotificationManager = this
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancelAll()
    }

    private fun initApp() {
        if (initFCM())
            Handler().postDelayed({
                log("Token >>>> After 2 sec checking token exists or not")
                if (!isFinishing && store.getString(Const.DEVICE_TOKEN) != null)
                    gotoMainActivity()
                else {
                    TokenRefresh.getInstance().setTokenListener(this)
                    waitFor15SecMore()
                }
            }, 2000)
    }

    private fun showUpdateDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update !!")
        builder.setMessage(intent.getStringExtra("updateMessage"))
        builder.setPositiveButton("Update") { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://play.google.com/store/apps/details?id=com.task.mobiadmin")
            startActivity(Intent.createChooser(intent, "Update using"))
            finish()
        }
        builder.setNegativeButton("Not now") { dialog, _ ->
            dialog.dismiss()
            initApp()
        }
        builder.create().show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelId = Const.NOTI_CHANNEL_ID
            val channelName = "Some Channel"
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun showDialogNoServices() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert !")
        builder.setMessage("Unable to initialize Google Services." +
                "\nMake sure your Internet connection speed is good." +
                "\nYou can try restart your phone, check if google apps like maps, gmail working or not." +
                "\nIf still problem persists then contact app admin")
        builder.setPositiveButton("Close") { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.setOnDismissListener { exitFromApp() }
        builder.show()
    }

    private fun waitFor15SecMore() {
        log("Token >>>> Token isn't here, Waiting for token for 15 sec")
        handler.postDelayed(waitingRunnable, 15000)
    }

    override fun onTokenRefresh() {
        log("Token >>>> Token is here")
        gotoMainActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(waitingRunnable)
    }

    private fun gotoMainActivity() {
        if (store.getString(Const.SESSION_ID) == null) {
            startActivity(Intent(this, LoginForgotActivity::class.java))
        } else
            startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
