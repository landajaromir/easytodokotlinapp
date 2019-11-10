package com.kotlin.education.android.easytodo.notifications

import android.content.Context
import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import com.evernote.android.job.JobManager

class ScheduleJobCreator : JobCreator {

    override fun create(tag: String): Job? {
        when (tag) {
            ScheduleNotificationJob.tag -> return ScheduleNotificationJob()
            else -> return null
        }
    }

    class AddReceiver : JobCreator.AddJobCreatorReceiver() {
        override fun addJobCreator(context: Context, manager: JobManager) {
            manager.addJobCreator(ScheduleJobCreator());
        }
    }
}