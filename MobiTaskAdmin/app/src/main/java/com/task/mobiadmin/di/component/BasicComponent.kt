package com.task.mobiadmin.di.component

import com.task.mobiadmin.activity.BaseActivity
import com.task.mobiadmin.di.module.AppModule

import javax.inject.Singleton

import dagger.Component

/**
 * Created by neeraj on 30/04/2018.
 */
@Singleton
@Component(modules = arrayOf(AppModule::class))
interface BasicComponent {
    fun inject(activity: BaseActivity)
}
