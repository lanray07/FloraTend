package com.floratend.app

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

object Reminders {
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(12, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("care-checks", ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as FloraApp
        if (!app.settings.reminders.first()) return Result.success()
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return Result.success()
        val manager = app.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("care", app.getString(R.string.care_reminders), NotificationManager.IMPORTANCE_DEFAULT))
        val due = app.database.dao().due(LocalDate.now().toString())
        if (due.isEmpty()) manager.cancel(100)
        else {
            val intent = PendingIntent.getActivity(app, 0, Intent(app, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            manager.notify(100, NotificationCompat.Builder(app, "care").setSmallIcon(R.drawable.ic_flora).setContentTitle(app.getString(R.string.app_name)).setContentText(app.resources.getQuantityString(R.plurals.reminder_count, due.size, due.size)).setContentIntent(intent).setAutoCancel(true).build())
        }
        return Result.success()
    }
}
