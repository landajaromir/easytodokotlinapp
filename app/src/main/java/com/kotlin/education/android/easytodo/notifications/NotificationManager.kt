package com.kotlin.education.android.easytodo.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import com.kotlin.education.android.easytodo.R
import com.kotlin.education.android.easytodo.activities.SplashScreenActivity
import com.kotlin.education.android.easytodo.activities.TaskDetailActivity
import com.kotlin.education.android.easytodo.constants.IntentConstants
import com.kotlin.education.android.easytodo.model.Task
import java.util.*
import com.kotlin.education.android.easytodo.activities.MainActivity
import com.kotlin.education.android.easytodo.notifications.ScheduleNotificationJob

/**
 * The main manager for showing the notification
 */
class NotificationManager {

    /**
     * The key for the group stacking
     */
    private val GROUP_KEY = "com.kotlin.education.android.easytodo.andr.group"
    private val CHANNEL_ID = "com.kotlin.education.android.easytodo.andr.channel_id"
    private val SUMMARY_ID = 0

    /**
     * Shows basic notification.
     * @param context    Context
     */
    fun showBasicNotification(context: Context, task: Task): Int {
        val notification = buildBasicNotification(context, task)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.resources.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setShowBadge(true)
            channel.enableLights(true)
            notificationManager.createNotificationChannel(channel)
        }

        var showGroup = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.activeNotifications != null && notificationManager.activeNotifications.size == 0) {
                showGroup = false
            }
        }

        val id = System.currentTimeMillis().toInt()
        notificationManager.notify(id, notification)

        if (showGroup) {
            val summary = buildSummary(context, GROUP_KEY)
            summary.defaults = 0
            summary.flags = summary.flags or Notification.FLAG_AUTO_CANCEL
            notificationManager.notify(SUMMARY_ID, summary)
        }
        return id
    }

    /**
     * Builds a basic notification.
     * @param context    Context
     * @return
     */
    private fun buildBasicNotification(context: Context, task: Task): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)

        builder.setContentTitle(context.getString(R.string.app_name))

        builder.setContentText(task.name)
        builder.setContentTitle(context.getString(R.string.app_name))

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        builder.setSound(soundUri)

        val notIntent = Intent(context, TaskDetailActivity::class.java)
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        notIntent.putExtra(IntentConstants.ID, task.id)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(notIntent)

        val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        builder.setContentIntent(pendingIntent)

        builder.setSmallIcon(R.drawable.ic_notification_icon)
            .setGroup(GROUP_KEY)
            .setChannelId(CHANNEL_ID)
            .setAutoCancel(true)

        return builder.build()
    }

    /**
     * Cancels all notifications.
     *
     * @param context Context
     */
    fun clearAllNotifications(context: Context) {
        val nm = NotificationManagerCompat.from(context)
        nm.cancelAll()
    }

    /**
     * Cancel one notification by it's id
     *
     * @param context Context
     * @param id      Notification id
     */
    fun clearNotification(context: Context, id: Int) {
        val nm = NotificationManagerCompat.from(context)
        nm.cancel(id)
    }

    /**
     * Schedules a notification for a specific time.
     * @param calendar   the time and date in the Calendar class.
     */
    fun scheduleNotification(calendar: Calendar, taskId: Long): Int {
        val extras = PersistableBundleCompat()
        extras.putLong(IntentConstants.ID, taskId)

        return JobRequest.Builder(ScheduleNotificationJob.tag)
            .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.EXPONENTIAL)
            .setExtras(extras)
            .setExact(calendar.timeInMillis - Calendar.getInstance().timeInMillis)
            .build()
            .schedule()
    }

    /**
     * Cancels scheduled reminder.
     *
     * @param jobID
     * @return return true if reminder is found and canceled.
     */
    fun cancelScheduledNotification(jobID: Int): Boolean {
        return JobManager.instance().cancel(jobID)
    }

    /**
     * Creates notification summary intent.
     *
     * @param context Context
     * @return
     */
    private fun getSummaryIntent(context: Context): PendingIntent {
        val intent = Intent(context, SplashScreenActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntentWithParentStack(intent)

        return PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Builds summary notification
     *
     * @param context  Context
     * @param groupKey
     * @return
     */
    @SuppressLint("ResourceType")
    private fun buildSummary(context: Context, groupKey: String): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        builder.setContentTitle(context.getString(R.string.app_name))
        builder.setContentText(context.getString(R.string.you_have_new_tasks))
        builder.setSmallIcon(R.drawable.ic_notification_icon)
        builder.setGroup(groupKey)
        builder.setChannelId(CHANNEL_ID)
        builder.setGroupSummary(true)
        builder.setContentIntent(getSummaryIntent(context))
        builder.setSound(null)
        return builder.build()
    }
    
    fun createNotificationCalendar(task: Task): Calendar {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = task.dueDate!! * 1000
        // TODO Homework: Make it so that user can choose the time of the notification.
        calendar.set(Calendar.HOUR, 9)
        calendar.set(Calendar.MINUTE, 0)
        return calendar
    }
}