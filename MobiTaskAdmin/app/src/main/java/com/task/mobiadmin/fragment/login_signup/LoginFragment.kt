package com.task.mobiadmin.fragment.login_signup

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.task.mobiadmin.BuildConfig
import com.task.mobiadmin.R
import com.task.mobiadmin.activity.MainActivity
import com.task.mobiadmin.data.UserData
import com.task.mobiadmin.fragment.BaseFragment
import com.task.mobiadmin.utils.Const
import com.task.mobiadmin.utils.MacUtils
import kotlinx.android.synthetic.main.fg_login.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class LoginFragment : BaseFragment() {

    private var loginCall: Call<JsonObject>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fg_login, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (BuildConfig.DEBUG) {
            emailET.setText("neeraj.n@pugmarks.com")
            passwordET.setText("admin123")
        }
        if (store.getBoolean(Const.REMEMBER_ME)) {
            rememberCB.isChecked = true
            emailET.setText(store.getString(Const.REMEMBER_EMAIL))
            passwordET.setText(store.getString(Const.REMEMBER_PASS))
        }
        backIV.setOnClickListener { baseActivity.onBackPressed() }
        loginBT.setOnClickListener { if (validate()) login() }
        forgotTV.setOnClickListener {
            baseActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_container_ls, ForgotPasswordFragment())
                    .addToBackStack(null)
                    .commit()
        }
    }

    private fun login() {
        val wifiMan = activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val mac = MacUtils.macAddress(wifiMan)
        val email = RequestBody.create(MediaType.parse("text/plain"), getText(emailET))
        val password = RequestBody.create(MediaType.parse("text/plain"), getText(passwordET))
        val deviceId = RequestBody.create(MediaType.parse("text/plain"), mac)
        loginCall = apiInterface.login(email, password, deviceId)
        apiManager.makeApiCall(loginCall, this)
    }

    private fun validate(): Boolean {
        when {
            getText(emailET).isEmpty() -> showToast("Please enter email", true)
            getText(passwordET).isEmpty() -> showToast("Please enter password", true)
            else -> return true
        }
        return false
    }

    override fun onSuccess(call: Call<*>, `object`: Any) {
        super.onSuccess(call, `object`)
        if (loginCall === call) {
            if (rememberCB.isChecked) {
                store.setBoolean(Const.REMEMBER_ME, true)
                store.saveString(Const.REMEMBER_EMAIL, getText(emailET))
                store.saveString(Const.REMEMBER_PASS, getText(passwordET))
            }
            val jsonObject = `object` as JsonObject
            val data = jsonObject.getAsJsonObject("data")
            val objectType = object : TypeToken<UserData>() {}.type
            val userData = Gson().fromJson<UserData>(data, objectType)
            store.saveString(Const.SESSION_ID, userData.sessionId)
            store.saveString(Const.USER_ID, userData.id)
            store.saveUserData(Const.USER_DATA, userData)
            gotoMainAct()
        }
    }

    private fun gotoMainAct() {
        startActivity(Intent(baseActivity, MainActivity::class.java))
        baseActivity.finish()
    }
}
