package com.task.mobiadmin.fragment.empMngmnt

import android.Manifest
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.google.gson.JsonObject
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.task.mobiadmin.R
import com.task.mobiadmin.activity.MainActivity
import com.task.mobiadmin.data.EmpData
import com.task.mobiadmin.fragment.BaseFragment
import com.task.mobiadmin.retrofitManager.ResponseListener
import com.task.mobiadmin.utils.Const
import com.task.mobiadmin.utils.ImageUtils
import com.task.mobiadmin.utils.PermissionsManager
import com.task.mobiadmin.utils.Utils
import kotlinx.android.synthetic.main.fg_emp_update.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class EmpUpdateFragment : BaseFragment(), PermissionsManager.PermissionCallback, ImageUtils.ImageSelectCallback {

    var updateCall: Call<JsonObject>? = null
    var updatePicCall: Call<JsonObject>? = null
    var deleteCall: Call<JsonObject>? = null
    var empData: EmpData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments.containsKey("data")) {
            empData = arguments.getParcelable("data")
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbar(true, "User Details")
        return inflater!!.inflate(R.layout.fg_emp_update, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }


    private fun initUI() {
        nameET.setText(empData?.name)
        phoneET.setText(empData?.phone)
        emailET.setText(empData?.email)
        loadPic()
        updateUserBT.setOnClickListener { if (validate()) updateUser() }
        userRIV.setOnClickListener {
            if (PermissionsManager.checkPermissions(baseActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 12, this))
                ImageUtils.ImageSelect.Builder(baseActivity, this, 13).crop().start()
        }
        deleteUserBT.setOnClickListener {
            val bldr = AlertDialog.Builder(baseActivity)
            bldr.setTitle("Delete User")
            bldr.setMessage("Are you sure you want to delete this user ?")
            bldr.setPositiveButton("Yes") { _, _ -> deleteUser() }
            bldr.setNegativeButton("No", null)
            bldr.create().show()
        }
    }

    private fun loadPic() {
        val target = MyTarget(userImagePB, userRIV)
        baseActivity.picasso.load(Const.ASSETS_BASE_URL + empData?.photo).noPlaceholder().into(target)
        userRIV.setTag(target)
    }

    private class MyTarget(userImagePB: ProgressBar, userRIV: RoundedImageView) : Target {

        val userImagePB: ProgressBar
        val userRIV: RoundedImageView

        init {
            this.userImagePB = userImagePB
            this.userRIV = userRIV
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {
        }

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            userImagePB.visibility = View.GONE
            userRIV.setImageBitmap(bitmap)
        }

    }

    private fun updateUser() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val userId = RequestBody.create(MediaType.parse("text/plain"), empData?.id.toString())
        val name = RequestBody.create(MediaType.parse("text/plain"), getText(nameET))
        val phone = RequestBody.create(MediaType.parse("text/plain"), getText(phoneET))
        updateCall = apiInterface.updateUser(sessionId, userId, name, phone)
        apiManager.makeApiCall(updateCall, this)
    }

    private fun updatePic(file: File) {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val userId = RequestBody.create(MediaType.parse("text/plain"), empData?.id.toString())
        var image: MultipartBody.Part? = null
        try {
            image = MultipartBody.Part.createFormData("upload", file.getName(), RequestBody.create(MediaType.parse("image/jpeg"), file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updatePicCall = apiInterface.updateUserPic(sessionId, userId, image)
        apiManager.makeApiCall(updatePicCall, false, this)
    }

    private fun deleteUser() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val userId = RequestBody.create(MediaType.parse("text/plain"), empData?.id.toString())
        deleteCall = apiInterface.deleteUser(sessionId, userId)
        apiManager.makeApiCall(deleteCall, this)
    }

    private fun validate(): Boolean {
        when {
            getText(nameET).isEmpty() -> showToast("Please enter User Name", true)
            getText(phoneET).isEmpty() -> showToast("Please enter Phone Number", true)
            else -> return true
        }
        return false
    }

    override fun onImageSelected(imagePath: String?, resultCode: Int) {
        val imageBitmap = ImageUtils.imageCompress(imagePath)
        val file = ImageUtils.bitmapToFile(imageBitmap, baseActivity)
        log("Filesize " + file?.length())
        updatePic(file)
        userImagePB.visibility = View.VISIBLE
    }

    override fun onPermissionsGranted(resultCode: Int) {
        ImageUtils.ImageSelect.Builder(baseActivity, this, 13).crop().start()
    }

    override fun onPermissionsDenied(resultCode: Int) {
    }

    override fun onSuccess(call: Call<*>?, `object`: Any?) {
        super.onSuccess(call, `object`)
        if (updateCall != null && updateCall === call) {
            showToast("User Updated Successfully")
            baseActivity.onBackPressed()
        } else if (updatePicCall != null && updatePicCall === call) {
            val jsonObject = `object` as JsonObject
            empData?.photo = jsonObject.get("data").asString
            loadPic()
        } else if (deleteCall != null && deleteCall === call) {
            showToast("User deleted successfully")
            (baseActivity as MainActivity).gotoHomeFragment()
        }
    }

    override fun onError(call: Call<*>?, statusCode: Int, errorMessage: String?, responseListener: ResponseListener?) {
        if (updatePicCall != null && updatePicCall === call) {
            showToast("Error in uploading the pic")
            userImagePB.visibility = View.GONE
        }
        if (statusCode == Const.ErrorCodes.USER_HAS_ACTIVE_TASK) {
            Utils.showAlert(baseActivity, "User has " + errorMessage + " active tasks\nPlease update or delete the tasks", null)
        } else
            super.onError(call, statusCode, errorMessage, responseListener)
    }
}