package com.floratend.app

import android.app.Notification
import android.app.NotificationManager
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

class LocalizedNotificationTest {
    @Test fun workerPublishesSelectedLocaleForEveryPackagedBundle() = runBlocking {
        val app=InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as FloraApp
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        val oldTag=AppLocales.selected(app);val oldEnabled=app.settings.reminders.first()
        val manager=app.getSystemService(NotificationManager::class.java)
        assertTrue(manager.areNotificationsEnabled())
        val plant=Plant(id="qa-localized-notification",name="Notification fixture")
        val rows=JSONArray()
        try {
            app.database.dao().save(plant)
            app.database.dao().save(CareSchedule(id="qa-localized-notification",plantId=plant.id))
            app.settings.set("reminders",true)
            for(tag in AppLocales.supported) {
                AppLocales.choose(app,tag)
                val request=OneTimeWorkRequestBuilder<ReminderWorker>().build()
                val work=WorkManager.getInstance(app)
                work.enqueue(request).result.get()
                val state=withTimeout(20000) { work.getWorkInfoByIdFlow(request.id).first { it?.state?.isFinished==true } }
                assertEquals(WorkInfo.State.SUCCEEDED,state?.state)
                val strings=AppLocales.wrap(app)
                val count=app.database.dao().due(java.time.LocalDate.now().toString()).size
                val expected=strings.resources.getQuantityString(R.plurals.reminder_count,count,count)
                withTimeout(5000) { while(manager.activeNotifications.singleOrNull { it.id==100 }?.notification?.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()!=expected) delay(50) }
                assertEquals(strings.getString(R.string.care_reminders),manager.getNotificationChannel("care").name.toString())
                rows.put(JSONObject().put("locale",tag).put("text",expected).put("state","SUCCEEDED"))
            }
            val folder=File(app.getExternalFilesDir(null),"localized-notifications").apply { mkdirs() }
            File(folder,"results.json").writeText(JSONObject().put("locales",rows).put("scope","Real WorkManager and OS notification text/channel; draft wording, no human grammar approval.").toString(2))
        } finally {
            app.database.dao().delete(plant);AppLocales.choose(app,oldTag);app.settings.set("reminders",oldEnabled)
            Reminders.refresh(app)
        }
    }
}
