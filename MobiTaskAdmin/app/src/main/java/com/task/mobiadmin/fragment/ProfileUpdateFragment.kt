package com.task.mobiadmin.fragment

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.task.mobiadmin.R
import com.task.mobiadmin.activity.MainActivity
import com.task.mobiadmin.data.UserData
import com.task.mobiadmin.utils.AppUtils
import com.task.mobiadmin.utils.Const
import com.task.mobiadmin.utils.ImageUtils
import com.task.mobiadmin.utils.PermissionsManager
import kotlinx.android.synthetic.main.fg_profile_update.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class ProfileUpdateFragment : BaseFragment(), PinAdd.AddPinListener, PermissionsManager.PermissionCallback, ImageUtils.ImageSelectCallback {

    private var file: File? = null
    private lateinit var userData: UserData
    private var updateCall: Call<JsonObject>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbar(true, "Update Profile")
        return inflater!!.inflate(R.layout.fg_profile_update, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        userData = store.getUserData(Const.USER_DATA, UserData::class.java) as UserData
        val target = AppUtils.MyTarget(userIV, baseActivity, false)
        baseActivity.picasso.load(Const.ASSETS_BASE_URL + userData.photo).placeholder(R.mipmap.ic_default_company).error(R.mipmap.ic_default_company).into(target)
        userIV.tag = target
        nameET.setText(userData.companyname)
        emailET.setText(userData.email)
        phoneET.setText(userData.phoneno)
        addressET.setText(userData.address)
        addLocIV.setOnClickListener {
            val pinAdd: PinAdd = PinAdd.instance
            pinAdd.setListener(this@ProfileUpdateFragment)
            baseActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_container, AddLocationPinFragment())
                    .addToBackStack(null)
                    .commit()
        }
        userIV.setOnClickListener {
            if (PermissionsManager.checkPermissions(baseActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 12, this))
                ImageUtils.ImageSelect.Builder(baseActivity, this, 13).crop().start()
        }
        AppUtils().loadStaticMap(baseActivity, locIV, userData.lat, userData.lng, true)
        saveBT.setOnClickListener { if (validate()) updateDetails() }
    }

    private fun validate(): Boolean {
        when {
            getText(nameET).isEmpty() -> showToast("Please enter Company Name", true)
            getText(phoneET).isEmpty() -> showToast("Please enter Phone Number", true)
            getText(addressET).isEmpty() -> showToast("Please enter Address", true)
            else -> return true
        }
        return false
    }

    private fun updateDetails() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val name = RequestBody.create(MediaType.parse("text/plain"), getText(nameET))
        val phone = RequestBody.create(MediaType.parse("text/plain"), getText(phoneET))
        val lat = RequestBody.create(MediaType.parse(" text/plain"), userData.lat)
        val lng = RequestBody.create(MediaType.parse(" text/plain"), userData.lng)
        val address = RequestBody.create(MediaType.parse("text/plain"), getText(addressET))
        var image: MultipartBody.Part? = null
        try {
            image = MultipartBody.Part.createFormData("pic", file?.name, RequestBody.create(MediaType.parse("image/jpeg"), file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updateCall = apiInterface.updateProfile(sessionId, name, phone, lat, lng, address, image)
        apiManager.makeApiCall(updateCall, this)
    }

    override fun onLocationPinAdded(address: String, latLng: LatLng) {
        Handler().postDelayed({
            baseActivity.runOnUiThread {
                saveBT.visibility = View.VISIBLE
                AppUtils().loadStaticMap(baseActivity, locIV, latLng.latitude.toString(), latLng.longitude.toString(), true)
                userData.lat = latLng.latitude.toString()
                userData.lng = latLng.longitude.toString()
                userData.address = address
                addressET.setText(userData.address)
            }
        }, 400)
    }

    override fun onSuccess(call: Call<*>?, `object`: Any?) {
        super.onSuccess(call, `object`)
        if (updateCall === call) {
            val jsonObject = `object` as JsonObject
            val data = jsonObject.getAsJsonObject("data")
            val objectType = object : TypeToken<UserData>() {
            }.type
            val userData = Gson().fromJson<UserData>(data, objectType)
            userData.sessionId = store.getString(Const.SESSION_ID)
            store.saveString(Const.USER_ID, userData.id)
            store.saveUserData(Const.USER_DATA, userData)

            (baseActivity as MainActivity).setUserData()

            showToast("Profile updated successfully")
            baseActivity.onBackPressed()
        }
    }

    override fun onPermissionsGranted(resultCode: Int) {
        ImageUtils.ImageSelect.Builder(baseActivity, this, 13).crop().start()
    }

    override fun onPermissionsDenied(resultCode: Int) {
    }


    override fun onImageSelected(imagePath: String?, resultCode: Int) {
        val imageBitmap = ImageUtils.imageCompress(imagePath)
        file = ImageUtils.bitmapToFile(imageBitmap, baseActivity)
        log("Filesize " + file?.length())
        userIV.setImageBitmap(imageBitmap)
    }
}