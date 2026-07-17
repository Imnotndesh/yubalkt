package com.imnotndesh.yubalkt.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.imnotndesh.yubalkt.MainActivity
import com.imnotndesh.yubalkt.core.network.YubalJob

class CancelJobReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val jobId = intent.getStringExtra(EXTRA_JOB_ID) ?: return
        val instanceUrl = intent.getStringExtra(EXTRA_INSTANCE_URL) ?: return
        kotlinx.coroutines.runBlocking {
            try {
                val client = com.imnotndesh.yubalkt.core.network.OkHttpYubalClient()
                val api = com.imnotndesh.yubalkt.core.network.YubalApiServiceImpl(client)
                api.cancelJob(instanceUrl, jobId)
            } catch (_: Exception) {}
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NotificationIdHelper.getNotificationId(jobId))
    }

    companion object {
        const val EXTRA_JOB_ID = "job_id"
        const val EXTRA_INSTANCE_URL = "instance_url"
        const val ACTION_CANCEL_JOB = "com.imnotndesh.yubalkt.CANCEL_JOB"
    }
}

object NotificationIdHelper {
    const val BASE_ID = 1001
    fun getNotificationId(jobId: String): Int = BASE_ID + jobId.hashCode() % 1000
}

class JobNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "yubal_jobs"
    }

    init { createChannels() }

    private fun createChannels() {
        val jobChannel = NotificationChannel(CHANNEL_ID, "Download Jobs", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Ongoing YouTube download jobs"
            setShowBadge(true)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(jobChannel)
    }

    fun showJobNotifications(activeJobs: List<YubalJob>, instanceUrl: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (activeJobs.isEmpty()) {
            activeJobs.forEach { manager.cancel(NotificationIdHelper.getNotificationId(it.id)) }
            return
        }

        activeJobs.forEach { job ->
            val title = job.contentInfo?.title ?: "Downloading..."
            val progress = job.progress.toInt().coerceIn(0, 100)

            val cancelIntent = Intent(context, CancelJobReceiver::class.java).apply {
                action = CancelJobReceiver.ACTION_CANCEL_JOB
                putExtra(CancelJobReceiver.EXTRA_JOB_ID, job.id)
                putExtra(CancelJobReceiver.EXTRA_INSTANCE_URL, instanceUrl)
            }
            val cancelPendingIntent = PendingIntent.getBroadcast(
                context, job.id.hashCode(), cancelIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val openPendingIntent = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText("${job.status.displayName} ($progress%)")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setOngoing(true)
                .setProgress(100, progress, false)
                .setContentIntent(openPendingIntent)
                .setSilent(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelPendingIntent)
                .build()

            manager.notify(NotificationIdHelper.getNotificationId(job.id), notification)
        }
    }

    fun cancelAll() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }
}
