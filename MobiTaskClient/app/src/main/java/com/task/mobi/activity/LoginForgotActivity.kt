package com.task.mobi.activity

import android.os.Bundle
import com.task.mobi.R
import com.task.mobi.fragment.LoginFragment

class LoginForgotActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_forgot)
        gotoLoginFragment()
    }

    private fun gotoLoginFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_container_lf, LoginFragment())
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
