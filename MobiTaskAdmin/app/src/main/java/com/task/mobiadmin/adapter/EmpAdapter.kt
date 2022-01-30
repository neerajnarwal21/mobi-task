package com.task.mobiadmin.adapter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.makeramen.roundedimageview.RoundedImageView
import com.task.mobiadmin.R
import com.task.mobiadmin.activity.BaseActivity
import com.task.mobiadmin.data.EmpData
import com.task.mobiadmin.fragment.empMngmnt.EmpUpdateFragment
import com.task.mobiadmin.fragment.home.TaskCreateFragment
import com.task.mobiadmin.utils.Const


class EmpAdapter(private var datas: ArrayList<EmpData>, private val baseActivity: BaseActivity) : RecyclerView.Adapter<EmpAdapter.MyViewHolder>() {
    private var data: ArrayList<EmpData>? = null

    init {
        data = datas
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_emp_list, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = datas.get(position)
        holder.nameTV.text = data.name
        holder.phoneTV.text = data.phone
        baseActivity.picasso.load(Const.ASSETS_BASE_URL + data.photo).placeholder(R.mipmap.ic_default_user).into(holder.userRIV)
        holder.mainCV.setOnClickListener {
            val frag = EmpUpdateFragment()
            val bundle = Bundle()
            bundle.putParcelable("data", data)
            frag.arguments = bundle
            baseActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_container, frag)
                    .addToBackStack(null)
                    .commit()
        }
        holder.newIV.setOnClickListener {
            val frag = TaskCreateFragment()
            val bundle = Bundle()
            bundle.putParcelable("data", data)
            frag.arguments = bundle
            baseActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_container, frag)
                    .addToBackStack(null)
                    .commit()
        }
        holder.phoneTV.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:" + data.phone)
            val chooser = Intent.createChooser(callIntent, "Call via")
            baseActivity.startActivity(chooser)
        }
    }

    override fun getItemCount() = datas.size

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal var nameTV: TextView
        internal var phoneTV: TextView
        internal var userRIV: RoundedImageView
        internal var mainCV: CardView
        internal var newIV: TextView

        init {
            nameTV = view.findViewById(R.id.nameTV)
            phoneTV = view.findViewById(R.id.phoneTV)
            userRIV = view.findViewById(R.id.userRIV)
            mainCV = view.findViewById(R.id.mainCV)
            newIV = view.findViewById(R.id.newIV)
        }
    }
}