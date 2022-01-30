package com.task.mobi.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonObject
import com.task.mobi.R
import com.task.mobi.utils.Const
import kotlinx.android.synthetic.main.fg_forgot_pass.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class ForgotPasswordFragment : BaseFragment() {

    private var forgotCall: Call<JsonObject>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fg_forgot_pass, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        submitBT.setOnClickListener({ if (validate()) resetPassword() })
        backIV.setOnClickListener({ baseActivity.onBackPressed() })
    }

    private fun resetPassword() {
        val email = RequestBody.create(MediaType.parse("text/plain"), getText(emailET))
        val userType = RequestBody.create(MediaType.parse("text/plain"), Const.USER)
        forgotCall = apiInterface.forgotPassword(email, userType)
        apiManager.makeApiCall(forgotCall, this)
    }

    private fun validate(): Boolean {
        if (getText(emailET).isEmpty()) {
            showToast("Please enter email", true)
        } else
            return true
        return false
    }

    override fun onSuccess(call: Call<*>, `object`: Any) {
        super.onSuccess(call, `object`)
        if (call === forgotCall) {
            baseActivity.onBackPressed()
            try {
                val jsonObject = `object` as JsonObject
                showToast(jsonObject.get("message").asString)
            } catch (e: Exception) {
                showToast("Please check you email for new password")
            }
        }
    }
}
