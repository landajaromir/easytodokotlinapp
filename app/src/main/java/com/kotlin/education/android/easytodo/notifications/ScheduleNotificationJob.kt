package com.kotlin.education.android.easytodo.notifications

import com.evernote.android.job.Job
import com.evernote.android.job.util.support.PersistableBundleCompat
import com.kotlin.education.android.easytodo.constants.IntentConstants
import com.kotlin.education.android.easytodo.database.TodoLocalRepository
import com.kotlin.education.android.easytodo.model.Task
import java.util.*

class ScheduleNotificationJob : Job() {

    override fun onRunJob(params: Job.Params): Job.Result {

        val extras: PersistableBundleCompat = params.extras
        val taskId: Long = extras.getLong(IntentConstants.ID, -1)
        if (taskId != -1L) {
            val task: Task = TodoLocalRepository().getById(taskId)
            NotificationManager().showBasicNotification(context, task)
        }
        return Job.Result.SUCCESS
    }

    companion object {

        val tag: String = Calendar.getInstance().timeInMillis.toString()
    }
}