package com.task.mobiadmin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonObject
import com.task.mobiadmin.R
import com.task.mobiadmin.utils.Const
import kotlinx.android.synthetic.main.fg_new_password.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class NewPasswordFragment : BaseFragment() {

    private var updateCall: Call<JsonObject>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbar(true, "Change Password")
        return inflater!!.inflate(R.layout.fg_new_password, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        submitBT.setOnClickListener { if (validate()) updatePassword() }
    }

    private fun updatePassword() {
        val sessId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val oldPass = RequestBody.create(MediaType.parse("text/plain"), getText(oldPassET))
        val newPass = RequestBody.create(MediaType.parse("text/plain"), getText(newPassET))

        updateCall = apiInterface.changePassword(sessId, oldPass, newPass)
        apiManager.makeApiCall(updateCall, this)
    }

    private fun validate(): Boolean {
        when {
            getText(oldPassET).isEmpty() -> showToast("Please enter Old Password", true)
            getText(newPassET).isEmpty() -> showToast("Please enter New Password", true)
            getText(confirmET).isEmpty() -> showToast("Please enter Confirm Password", true)
            getText(newPassET) != getText(confirmET) -> showToast("Passwords doesn't match")
            else -> return true
        }
        return false
    }

    override fun onSuccess(call: Call<*>, `object`: Any) {
        super.onSuccess(call, `object`)
        if (updateCall === call) {
            showToast("Password Updated Successfully")
            baseActivity.onBackPressed()
        }
    }
}
