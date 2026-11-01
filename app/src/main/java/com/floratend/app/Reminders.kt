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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.util.concurrent.TimeUnit

object Reminders {
    val lock = Mutex()
    private val schedulingLock=Mutex()
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
    fun refresh(context: Context) {
        val app=context.applicationContext as FloraApp
        scope.launch { synchronize(app,refresh=true) }
    }
    fun schedule(context: Context) {
        val app=context.applicationContext as FloraApp
        scope.launch {
            app.settings.reminders.distinctUntilChanged().collect { synchronize(app,refresh=false) }
        }
    }
    private suspend fun synchronize(app:FloraApp,refresh:Boolean)=schedulingLock.withLock {
        val work=WorkManager.getInstance(app)
        // Read inside the scheduling lock: a queued refresh must not resurrect disabled work.
        if(!app.settings.reminders.first()) {
            lock.withLock {
                work.cancelUniqueWork("care-checks")
                work.cancelUniqueWork("care-refresh")
                app.getSystemService(NotificationManager::class.java).cancel(100)
            }
        } else {
            work.enqueueUniquePeriodicWork("care-checks",ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<ReminderWorker>(12,TimeUnit.HOURS).build())
            if(refresh) work.enqueueUniqueWork("care-refresh",ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<ReminderWorker>().build())
        }
    }
}
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = Reminders.lock.withLock {
        val app = applicationContext as FloraApp
        val strings=AppLocales.wrap(app)
        val manager = app.getSystemService(NotificationManager::class.java)
        if (!app.settings.reminders.first()) { manager.cancel(100); return@withLock Result.success() }
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) { manager.cancel(100); return@withLock Result.success() }
        manager.createNotificationChannel(NotificationChannel("care", strings.getString(R.string.care_reminders), NotificationManager.IMPORTANCE_DEFAULT))
        val due = app.database.dao().due(LocalDate.now().toString())
        if (due.isEmpty()) manager.cancel(100)
        else {
            val intent = PendingIntent.getActivity(app, 0, Intent(app, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            manager.notify(100, NotificationCompat.Builder(app, "care").setSmallIcon(R.drawable.ic_flora_monochrome).setContentTitle(strings.getString(R.string.app_name)).setContentText(strings.resources.getQuantityString(R.plurals.reminder_count, due.size, due.size)).setContentIntent(intent).setAutoCancel(true).build())
        }
        Result.success()
    }
}
