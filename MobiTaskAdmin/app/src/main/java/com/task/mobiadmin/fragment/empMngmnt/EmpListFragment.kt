package com.task.mobiadmin.fragment.empMngmnt

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.task.mobiadmin.R
import com.task.mobiadmin.adapter.EmpAdapter
import com.task.mobiadmin.data.EmpData
import com.task.mobiadmin.fragment.BaseFragment
import com.task.mobiadmin.retrofitManager.ResponseListener
import com.task.mobiadmin.utils.Const
import kotlinx.android.synthetic.main.fg_emp_list.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call

/**
 * Created by neeraj on 2/4/18.
 */

class EmpListFragment : BaseFragment() {
    private var adapter: EmpAdapter? = null
    private var empCall: Call<JsonObject>? = null
    var datas = ArrayList<EmpData>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbar(true, "Users")
        return inflater!!.inflate(R.layout.fg_emp_list, container, false) as ViewGroup
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listRV.layoutManager = LinearLayoutManager(activity)
        newIV.setOnClickListener {
            baseActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_container, EmpCreateFragment())
                    .addToBackStack(null)
                    .commit()
        }
        newFirstIV.setOnClickListener {
            baseActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_container, EmpCreateFragment())
                    .addToBackStack(null)
                    .commit()
        }
        getEmpList()
    }

    private fun getEmpList() {
        val sessionId = RequestBody.create(MediaType.parse("text/plain"), store.getString(Const.SESSION_ID))
        empCall = apiInterface.listUsers(sessionId)
        apiManager.makeApiCall(empCall, this)
    }

    override fun onSuccess(call: Call<*>?, `object`: Any?) {
        super.onSuccess(call, `object`)
        if (empCall === call) {
            recyclerVCL.visibility = View.VISIBLE
            val jsonObject = `object` as JsonObject
            val data = jsonObject.getAsJsonArray("data")
            val objectType = object : TypeToken<ArrayList<EmpData>>() {}.type
            datas = Gson().fromJson<ArrayList<EmpData>>(data, objectType)

            adapter = EmpAdapter(datas, baseActivity)
            listRV.adapter = adapter
        }
    }

    override fun onError(call: Call<*>?, statusCode: Int, errorMessage: String?, responseListener: ResponseListener?) {
        if (statusCode == Const.ErrorCodes.NO_USER) {
            newVCL.visibility = View.VISIBLE
        } else
            super.onError(call, statusCode, errorMessage, responseListener)
    }
}