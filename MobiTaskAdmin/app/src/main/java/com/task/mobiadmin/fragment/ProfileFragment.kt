package com.task.mobiadmin.fragment

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
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
import com.task.mobiadmin.utils.Utils
import kotlinx.android.synthetic.main.fg_profile.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class ProfileFragment : BaseFragment(), PinAdd.AddPinListener {

    private lateinit var userData: UserData
    private var updateCall: Call<JsonObject>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbar(false, "")
        return inflater!!.inflate(R.layout.fg_profile, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        baseActivity.setSupportActionBar(toolBarProfile)
        toolBarProfile.setNavigationOnClickListener { baseActivity.onBackPressed() }
        initUI()
    }

    private fun initUI() {
        userData = store.getUserData(Const.USER_DATA, UserData::class.java) as UserData
        activeTasksTV.text = (baseActivity as MainActivity).getTotalTasksCount() + " Active tasks"
        val target = AppUtils.MyTarget(userIV, baseActivity, false)
        baseActivity.picasso.load(Const.ASSETS_BASE_URL + userData.photo).placeholder(R.mipmap.ic_default_company).error(R.mipmap.ic_default_company).into(target)
        userIV.tag = target
        toolBarProfile.title = userData.companyname
        emailTV.text = userData.email
        phoneTV.text = userData.phoneno
        addressTV.text = userData.address
        expiryDateTV.text = "Active until : " + Utils.changeDateFormat(userData.expiryTime, "yyyy-MM-dd HH:mm:ss", "MMM dd, yyyy")
        if (userData.lat.isEmpty()) {
            addLocCL.visibility = View.VISIBLE
            addLocCL.setOnClickListener {
                val pinAdd: PinAdd = PinAdd.instance
                pinAdd.setListener(this@ProfileFragment)
                baseActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_container, AddLocationPinFragment())
                        .addToBackStack(null)
                        .commit()
            }
        } else AppUtils().loadStaticMap(baseActivity, locIV, userData.lat, userData.lng, true)

        editIV.setOnClickListener {
            baseActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_container, ProfileUpdateFragment())
                    .addToBackStack(null)
                    .commit()
        }
        saveBT.setOnClickListener { updateDetails() }
        passBT.setOnClickListener {
            baseActivity
                    .supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_container, NewPasswordFragment())
                    .addToBackStack(null)
                    .commit()
        }
    }

    private fun updateDetails() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        val name = RequestBody.create(MediaType.parse("text/plain"), userData.companyname)
        val phone = RequestBody.create(MediaType.parse("text/plain"), userData.phoneno)
        val lat = RequestBody.create(MediaType.parse(" text/plain"), userData.lat)
        val lng = RequestBody.create(MediaType.parse(" text/plain"), userData.lng)
        val address = RequestBody.create(MediaType.parse("text/plain"), userData.address)
        val image: MultipartBody.Part? = null
        updateCall = apiInterface.updateProfile(sessionId, name, phone, lat, lng, address, image)
        apiManager.makeApiCall(updateCall, this)
    }

    override fun onLocationPinAdded(address: String, latLng: LatLng) {
        Handler().postDelayed({
            baseActivity.runOnUiThread {
                addLocCL.visibility = View.GONE
                saveBT.visibility = View.VISIBLE
                AppUtils().loadStaticMap(baseActivity, locIV, latLng.latitude.toString(), latLng.longitude.toString(), true)
                userData.lat = latLng.latitude.toString()
                userData.lng = latLng.longitude.toString()
                showAddressDialog(address)
            }
        }, 400)
    }

    private fun showAddressDialog(address: String) {
        val bldr = AlertDialog.Builder(baseActivity)
        bldr.setTitle("Address")
        bldr.setMessage("Do you also want to update address \n\n${userData.address}\n\n with \n\n$address\n")
        bldr.setPositiveButton("Update") { _, _ ->
            userData.address = address
            addressTV.text = userData.address
        }
        bldr.setCancelable(false)
        bldr.setNegativeButton("No", null)
        bldr.create().show()
    }

    override fun onSuccess(call: Call<*>?, `object`: Any?) {
        super.onSuccess(call, `object`)
        if (updateCall === call) {

            val jsonObject = `object` as JsonObject
            val data = jsonObject.getAsJsonObject("data")
            val objectType = object : TypeToken<UserData>() {}.type
            val userData = Gson().fromJson<UserData>(data, objectType)
            userData.sessionId = store.getString(Const.SESSION_ID)
            store.saveString(Const.USER_ID, userData.id)
            store.saveUserData(Const.USER_DATA, userData)

            (baseActivity as MainActivity).setUserData()

            showToast("Profile updated successfully")
            baseActivity.onBackPressed()
        }
    }
}
