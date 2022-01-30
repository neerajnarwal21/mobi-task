package com.task.mobiadmin.fragment.login_signup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.task.mobiadmin.R
import com.task.mobiadmin.fragment.BaseFragment

import kotlinx.android.synthetic.main.fg_login_sign_choose.*

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class LoginSignUpChooseFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fg_login_sign_choose, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signUpBT.setOnClickListener { gotoFragment(SignupFragment()) }
        loginBT.setOnClickListener { gotoFragment(LoginFragment()) }
    }

    private fun gotoFragment(fragment: Fragment) {
        baseActivity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_container_ls, fragment)
                .addToBackStack(null)
                .commit()
    }
}
