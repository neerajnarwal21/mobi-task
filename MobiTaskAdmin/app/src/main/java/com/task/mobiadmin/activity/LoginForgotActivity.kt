package com.task.mobiadmin.activity

import android.os.Bundle
import com.task.mobiadmin.R
import com.task.mobiadmin.fragment.login_signup.LoginSignUpChooseFragment

class LoginForgotActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_forgot)
        gotoChooseFragment()
    }

    private fun gotoChooseFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_container_ls, LoginSignUpChooseFragment())
                .commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            exitFromApp()
        }
    }
}
