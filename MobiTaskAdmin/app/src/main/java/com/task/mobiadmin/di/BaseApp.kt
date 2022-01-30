package com.task.mobiadmin.di

import android.app.Application

import com.task.mobiadmin.di.component.BasicComponent
import com.task.mobiadmin.di.component.DaggerBasicComponent
import com.task.mobiadmin.di.module.AppModule


/**
 * Created by neeraj on 30/04/2018.
 */

class BaseApp : Application() {
    private lateinit var basicComponent: BasicComponent

    override fun onCreate() {
        super.onCreate()
        app = this

        basicComponent = DaggerBasicComponent.builder()
                .appModule(AppModule(applicationContext))
                .build()
    }

    fun getInjection() = basicComponent

    companion object {
        private lateinit var app: BaseApp
        fun create() = app
    }
}
