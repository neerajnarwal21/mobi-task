package com.task.mobiadmin.fragment.home

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.EditText
import com.task.mobiadmin.R
import com.task.mobiadmin.activity.MainActivity
import com.task.mobiadmin.adapter.PageAdapter
import com.task.mobiadmin.fragment.BaseFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fg_home.*


/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class HomeFragment : BaseFragment(), SearchView.OnQueryTextListener, TabCountListener.NewTabCount, TabCountListener.OngoingTabCount, TabCountListener.CompleteTabCount, TabLayout.OnTabSelectedListener {

    private var vieww: View? = null
    private lateinit var searchView: SearchView
    private var menuItem: MenuItem? = null
    private var hashMap = hashMapOf<Int, Fragment>()
    private var activeFrag: Fragment? = null
    private var isInflated = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbar(false, getString(R.string.app_name))
//        if (vieww == null) {
//            vieww =
//            isInflated = true
//        }
        return inflater!!.inflate(R.layout.fg_home, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        Handler().postDelayed({
            try {
                baseActivity.setSupportActionBar(toolBarHome)
                toolBarHome.setNavigationOnClickListener {
                    (baseActivity as MainActivity).drawer.openDrawer(GravityCompat.START)
                }
                baseActivity.invalidateOptionsMenu()
            } catch (ignored: Exception) {
            }
        }, 400)
//        if (isInflated) {
//            isInflated = false
        initUI()
        newFAB.setOnClickListener {
            baseActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_container, TaskCreateFragment())
//                    .addToBackStack(null)
                    .commit()
        }
//        }
    }

    private fun initUI() {
        setupViewPager()
        pager.offscreenPageLimit = 2
        tabs.setupWithViewPager(pager)
        tabs.getTabAt(1)?.select()
        activeFrag = hashMap[1]
    }

    override fun onResume() {
        super.onResume()
        tabs.addOnTabSelectedListener(this)
    }

    override fun onPause() {
        super.onPause()
        tabs.removeOnTabSelectedListener(this)
        try {
            if (!searchView.isIconified) {
                MenuItemCompat.collapseActionView(menuItem)
            }
        } catch (ignored: Exception) {
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        if (!searchView.isIconified) {
            MenuItemCompat.collapseActionView(menuItem)
        }
        activeFrag = hashMap[tab.position]
    }

    private fun setupViewPager() {
        val adapter = PageAdapter(childFragmentManager)
        if (hashMap.isEmpty()) {
            val frag = ListNewFragment()
            val frag1 = ListOngoingFragment()
            val frag2 = ListCompleteFragment()
            hashMap[0] = frag
            hashMap[1] = frag1
            hashMap[2] = frag2
            frag.setOnCountListener(this)
            frag1.setOnCountListener(this)
            frag2.setOnCountListener(this)
        }
        adapter.addFragment(hashMap[0]!!, "New")
        adapter.addFragment(hashMap[1]!!, "Ongoing")
        adapter.addFragment(hashMap[2]!!, "Completed")
        pager.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.menu_save, menu)
        menuItem = menu!!.findItem(R.id.action_search)
        searchView = menuItem!!.actionView as SearchView
        val searchEditText = searchView.findViewById<EditText>(android.support.v7.appcompat.R.id.search_src_text)
        searchEditText.hint = "Task Title or User Name"
        searchEditText.setHintTextColor(ContextCompat.getColor(context, R.color.White_semi_trans))
        searchEditText.setTextColor(ContextCompat.getColor(context, R.color.White))
        searchView.setOnQueryTextListener(this)
        menuItem!!.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                // send reset list callback from here
                when (activeFrag) {
                    is ListNewFragment -> (activeFrag as ListNewFragment).onResetList()
                    is ListOngoingFragment -> (activeFrag as ListOngoingFragment).onResetList()
                    is ListCompleteFragment -> (activeFrag as ListCompleteFragment).onResetList()
                }
                return true
            }
        })
    }

    override fun onQueryTextChange(newText: String): Boolean {
        // send query text in each list from here
        when (activeFrag) {
            is ListNewFragment -> (activeFrag as ListNewFragment).onQueryTextChange(newText)
            is ListOngoingFragment -> (activeFrag as ListOngoingFragment).onQueryTextChange(newText)
            is ListCompleteFragment -> (activeFrag as ListCompleteFragment).onQueryTextChange(newText)
        }
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onCountChangeInNew() {
        //TODO if needed to change in tab header view
    }

    override fun onCountChangeInOngoing() {

    }

    override fun onCountChangeInComplete() {

    }
}

