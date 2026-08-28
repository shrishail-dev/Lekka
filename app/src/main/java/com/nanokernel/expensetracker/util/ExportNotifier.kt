package com.nanokernel.expensetracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

const val EXPORT_NOTIFICATION_CHANNEL_ID = "downloads"

fun createExportNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val channel = NotificationChannel(
        EXPORT_NOTIFICATION_CHANNEL_ID,
        "Downloads",
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply { description = "Notifies when an expense export finishes saving" }
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
}

/** Posts a notification for a finished export; tapping it opens the saved file. */
fun notifyExportComplete(context: Context, result: ExportResult) {
    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(result.uri, result.mimeType)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    val pendingIntent = android.app.PendingIntent.getActivity(
        context,
        result.fileName.hashCode(),
        viewIntent,
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, EXPORT_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_sys_download_done)
        .setContentTitle("Export saved")
        .setContentText("${result.fileName} — tap to open")
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    runCatching { NotificationManagerCompat.from(context).notify(result.fileName.hashCode(), notification) }
}
