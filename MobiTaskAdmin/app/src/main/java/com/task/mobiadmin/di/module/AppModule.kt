package com.task.mobiadmin.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by neeraj on 30/04/2018.
 */
@Module
class AppModule(private val context: Context) {

    @Singleton
    @Provides
    fun provideContext() = context

    //    @Singleton
    //    @Provides
    //    public SharedPreferences provideSharedPreferences(Context context) {
    //        return PreferenceManager.getDefaultSharedPreferences(context);
    //    }
    //
    //    @Singleton
    //    @Provides
    //    public Gson provideGson() {
    //        return new Gson();
    //    }
}
