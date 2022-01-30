package com.task.mobiadmin.fragment.empMngmnt

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonObject
import com.task.mobiadmin.R
import com.task.mobiadmin.fragment.BaseFragment
import com.task.mobiadmin.utils.Const
import com.task.mobiadmin.utils.ImageUtils
import com.task.mobiadmin.utils.PermissionsManager
import com.task.mobiadmin.utils.Utils
import kotlinx.android.synthetic.main.fg_emp_create.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class EmpCreateFragment : BaseFragment(), PermissionsManager.PermissionCallback, ImageUtils.ImageSelectCallback {

    var file: File? = null
    var createCall: Call<JsonObject>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbar(true, "Create User")
        return inflater!!.inflate(R.layout.fg_emp_create, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }


    private fun initUI() {
        createUserBT.setOnClickListener { if (validate()) createUser() }
        userRIV.setOnClickListener {
            if (PermissionsManager.checkPermissions(baseActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 12, this))
                ImageUtils.ImageSelect.Builder(baseActivity, this, 13).crop().start()
        }
    }

    private fun createUser() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val name = RequestBody.create(MediaType.parse("text/plain"), getText(nameET))
        val phone = RequestBody.create(MediaType.parse("text/plain"), getText(phoneET))
        val email = RequestBody.create(MediaType.parse("text/plain"), getText(emailET))
        val password = RequestBody.create(MediaType.parse("text/plain"), getText(passwordET))
        var image: MultipartBody.Part? = null
        try {
            image = MultipartBody.Part.createFormData("upload", file?.getName(), RequestBody.create(MediaType.parse("image/jpeg"), file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        createCall = apiInterface.createUser(sessionId, name, phone, email, password, image)
        apiManager.makeApiCall(createCall, this)
    }

    private fun validate(): Boolean {
        when {
            getText(nameET).isEmpty() -> showToast("Please enter User Name", true)
            getText(emailET).isEmpty() -> showToast("Please enter your Email Address", true)
            !Utils.isValidMail(getText(emailET)) -> showToast("It is not a valid Email Address", true)
            getText(passwordET).isEmpty() -> showToast("Please enter Password", true)
            !Utils.isValidPassword(getText(passwordET)) -> showToast("Password should be 8 char long", true)
            getText(confPasswordET).isEmpty() -> showToast("Please confirm Password", true)
            !getText(passwordET).equals(getText(confPasswordET)) -> showToast("Passwords does not match", true)
            else -> return true
        }
        return false
    }

    override fun onImageSelected(imagePath: String?, resultCode: Int) {
        val imageBitmap = ImageUtils.imageCompress(imagePath)
        file = ImageUtils.bitmapToFile(imageBitmap, baseActivity)
        log("Filesize " + file?.length())
        userRIV.setImageBitmap(imageBitmap)
    }

    override fun onPermissionsGranted(resultCode: Int) {
        ImageUtils.ImageSelect.Builder(baseActivity, this, 13).crop().start()
    }

    override fun onPermissionsDenied(resultCode: Int) {
    }

    override fun onSuccess(call: Call<*>?, `object`: Any?) {
        super.onSuccess(call, `object`)
        showToast("User Created Successfully")
        baseActivity.onBackPressed()
    }
}