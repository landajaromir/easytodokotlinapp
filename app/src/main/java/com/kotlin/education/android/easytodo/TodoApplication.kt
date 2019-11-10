package com.kotlin.education.android.easytodo

import android.app.Application
import android.content.Context
import com.evernote.android.job.JobManager
import com.kotlin.education.android.easytodo.notifications.ScheduleJobCreator


/**
 * The main class representing the instance of a application.
 * The class is initialized when the application starts.
 * It can be used to keep global information during the runtime of the application.
 *
 * Documentation:
 * https://developer.android.com/reference/android/app/Application
 *
 * TODO Homework: Have a look at https://square.github.io/leakcanary/fundamentals/.
 */
class TodoApplication : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: TodoApplication? = null

        /**
         * Returns the application context which is not dependent on any activity.
         * Very useful for getting resources in fragments.
         * @return application context
         */
        fun getAppContent() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Just when the app is created, we initialize the JobManager.
        // JobManager will be used to schedule notifications.
        JobManager.create(this).addJobCreator(ScheduleJobCreator())

    }
}