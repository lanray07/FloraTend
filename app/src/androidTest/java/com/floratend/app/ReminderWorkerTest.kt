package com.floratend.app

import android.app.Notification
import android.app.NotificationManager
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.time.LocalDate

class ReminderWorkerTest {
    @Test fun workerUsesOneNotificationAndUpdatesAfterCompletion() = runBlocking {
        val app=InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as FloraApp
        val manager=app.getSystemService(NotificationManager::class.java)
        assumeTrue("Device notification settings must permit notifications",manager.areNotificationsEnabled())
        val original=app.settings.reminders.first()
        val plant=Plant(id="acceptance-worker-plant",name="FloraTend QA reminder")
        val schedule=CareSchedule(id="acceptance-worker",plantId=plant.id)
        suspend fun execute() {
            val request=OneTimeWorkRequestBuilder<ReminderWorker>().build()
            val work=WorkManager.getInstance(app)
            work.enqueue(request).result.get()
            val finished=withTimeout(20000) { work.getWorkInfoByIdFlow(request.id).first { it?.state?.isFinished==true } }
            assertEquals(WorkInfo.State.SUCCEEDED,finished?.state)
        }
        try {
            app.database.dao().save(plant);app.database.dao().save(schedule)
            app.settings.set("reminders",true)
            execute();execute()
            assertEquals(1,manager.activeNotifications.count { it.id==100 })
            val count=app.database.dao().due(LocalDate.now().toString()).size
            assertEquals(app.resources.getQuantityString(R.plurals.reminder_count,count,count),manager.activeNotifications.single { it.id==100 }.notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString())
            app.database.dao().complete(schedule,LocalDate.now(),"Soil checked: moist")
            execute()
            val remaining=app.database.dao().due(LocalDate.now().toString()).size
            if(remaining==0) assertTrue(manager.activeNotifications.none { it.id==100 })
            else assertEquals(app.resources.getQuantityString(R.plurals.reminder_count,remaining,remaining),manager.activeNotifications.single { it.id==100 }.notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString())
        } finally {
            app.database.dao().delete(plant)
            app.settings.set("reminders",original)
            if(!original) manager.cancel(100)
        }
    }
}
