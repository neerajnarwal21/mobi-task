package com.task.mobiadmin.fragment.login_signup

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonObject
import com.task.mobiadmin.R
import com.task.mobiadmin.fragment.BaseFragment
import com.task.mobiadmin.utils.Const
import com.task.mobiadmin.utils.MacUtils
import com.task.mobiadmin.utils.Utils
import kotlinx.android.synthetic.main.fg_signup.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call


/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class SignupFragment : BaseFragment() {

    private var signupCall: Call<JsonObject>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fg_signup, null)
    }

    private var mac: String = ""

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backIV.setOnClickListener { baseActivity.onBackPressed() }
        val wifiMan = activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mac = MacUtils.macAddress(wifiMan)

        signupBT.setOnClickListener { if (validate()) signup() }
    }

    private fun signup() {
        val name = RequestBody.create(MediaType.parse("text/plain"), getText(nameET))
        val phone = RequestBody.create(MediaType.parse("text/plain"), getText(phoneET))
        val address = RequestBody.create(MediaType.parse("text/plain"), getText(addressET))
        val email = RequestBody.create(MediaType.parse("text/plain"), getText(emailET))
        val password = RequestBody.create(MediaType.parse("text/plain"), getText(passwordET))
        val token = RequestBody.create(MediaType.parse("text/plain"), baseActivity.store.getString(Const.DEVICE_TOKEN))
        val userLimit = RequestBody.create(MediaType.parse("text/plain"), "2")
        val userType = RequestBody.create(MediaType.parse("text/plain"), "email")
        val deviceId = RequestBody.create(MediaType.parse("text/plain"), mac)
        val submit = RequestBody.create(MediaType.parse("text/plain"), "")
        signupCall = apiInterface.signup(name, phone, address, email, password, token, userLimit, userType, deviceId, submit)
        apiManager.makeApiCall(signupCall, this)
    }

    private fun validate(): Boolean {
        when {
            getText(nameET).isEmpty() -> showToast("Please enter Company Name", true)
            getText(phoneET).isEmpty() -> showToast("Please enter Phone Number", true)
            getText(addressET).isEmpty() -> showToast("Please enter Company's Address", true)
            getText(emailET).isEmpty() -> showToast("Please enter your Email Address", true)
            !Utils.isValidMail(getText(emailET)) -> showToast("It is not a valid Email Address", true)
            getText(passwordET).isEmpty() -> showToast("Please enter Password", true)
            !Utils.isValidPassword(getText(passwordET)) -> showToast("Password should be 8 char long", true)
            getText(confPasswordET).isEmpty() -> showToast("Please confirm Password", true)
            getText(passwordET) != getText(confPasswordET) -> showToast("Passwords does not match", true)
            else -> return true
        }
        return false
    }

    override fun onSuccess(call: Call<*>, `object`: Any) {
        super.onSuccess(call, `object`)
        if (signupCall === call) {
            val jsonObject = `object` as JsonObject
            try {
                Utils.showAlert(baseActivity, jsonObject.get("message").asString) { baseActivity.onBackPressed() }
            } catch (ignored: Exception) {
            }
        }
    }
}