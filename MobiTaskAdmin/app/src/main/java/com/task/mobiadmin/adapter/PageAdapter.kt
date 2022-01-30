package com.task.mobiadmin.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Created by neeraj on 4/4/18.
 */
class PageAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

    private val mFragmentList = arrayListOf<Fragment>()
    private val mFragmentTitleList = arrayListOf<String>()

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    override fun getItem(position: Int) = mFragmentList.get(position)

    override fun getCount() = mFragmentList.size

    override fun getPageTitle(position: Int) = mFragmentTitleList.get(position)
}