package com.infinity.linkleap.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val manager = NotificationManagerCompat.from(context)
    private var lastUpdateMs = 0L

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(CH_PROGRESS, "Progres transfer", NotificationManager.IMPORTANCE_LOW)
            )
            nm.createNotificationChannel(
                NotificationChannel(CH_RESULT, "Hasil transfer", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }

    fun showProgress(isSending: Boolean, sent: Long, total: Long, speedBps: Long, etaSec: Long) {
        val percent = if (total > 0) (sent * 100 / total).toInt() else 0
        val now = SystemClock.elapsedRealtime()
        // Batasi update agar tidak membanjiri sistem (maks ±2x per detik)
        if (percent < 100 && now - lastUpdateMs < 500) return
        lastUpdateMs = now

        val detail = buildString {
            append("${formatBytes(sent)} / ${formatBytes(total)}")
            if (speedBps > 0) append(" • ${formatBytes(speedBps)}/dtk")
            if (etaSec >= 0) append(" • sisa ${formatEta(etaSec)}")
        }

        val notification = NotificationCompat.Builder(context, CH_PROGRESS)
            .setSmallIcon(
                if (isSending) android.R.drawable.stat_sys_upload
                else android.R.drawable.stat_sys_download
            )
            .setContentTitle("${if (isSending) "Mengirim" else "Menerima"} file • $percent%")
            .setContentText(detail)
            .setProgress(100, percent, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(openAppIntent())
            .build()
        notify(ID_PROGRESS, notification)
    }

    fun showDone(isSending: Boolean, text: String) {
        cancelProgress()
        val notification = NotificationCompat.Builder(context, CH_RESULT)
            .setSmallIcon(
                if (isSending) android.R.drawable.stat_sys_upload_done
                else android.R.drawable.stat_sys_download_done
            )
            .setContentTitle(if (isSending) "Pengiriman berhasil ✅" else "File berhasil diterima ✅")
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .build()
        notify(ID_RESULT, notification)
    }

    fun showFailed(isSending: Boolean, reason: String) {
        cancelProgress()
        val notification = NotificationCompat.Builder(context, CH_RESULT)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(if (isSending) "Pengiriman gagal ❌" else "Penerimaan gagal ❌")
            .setContentText(reason)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .build()
        notify(ID_RESULT, notification)
    }

    fun cancelProgress() {
        manager.cancel(ID_PROGRESS)
        lastUpdateMs = 0L
    }

    @SuppressLint("MissingPermission")
    private fun notify(id: Int, notification: Notification) {
        // false jika izin POST_NOTIFICATIONS ditolak (Android 13+)
        if (manager.areNotificationsEnabled()) manager.notify(id, notification)
    }

    private fun openAppIntent(): PendingIntent? {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: return null
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private companion object {
        const val CH_PROGRESS = "transfer_progress"
        const val CH_RESULT = "transfer_result"
        const val ID_PROGRESS = 1
        const val ID_RESULT = 2
    }
}