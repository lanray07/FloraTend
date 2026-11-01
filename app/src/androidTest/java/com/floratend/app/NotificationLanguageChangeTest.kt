package com.floratend.app

import android.app.Notification
import android.app.NotificationManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.work.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import java.util.Locale

class NotificationLanguageChangeTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun choosingLanguageRefreshesExistingCareNotification() = runBlocking {
        val app=rule.activity.application as FloraApp
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        val old=AppLocales.selected(app);val enabled=app.settings.reminders.first()
        val plant=Plant(id="qa-notification-language",name="Language fixture")
        val manager=app.getSystemService(NotificationManager::class.java)
        try {
            AppLocales.choose(app,"en-GB");app.settings.set("onboarded",true);app.settings.set("reminders",true)
            app.database.dao().save(plant);app.database.dao().save(CareSchedule(id=plant.id,plantId=plant.id))
            val work=WorkManager.getInstance(app);val request=OneTimeWorkRequestBuilder<ReminderWorker>().build()
            work.enqueue(request).result.get()
            withTimeout(15000) { work.getWorkInfoByIdFlow(request.id).first { it?.state?.isFinished==true } }
            val count=app.database.dao().due(java.time.LocalDate.now().toString()).size
            val english=AppLocales.wrap(app).resources.getQuantityString(R.plurals.reminder_count,count,count)
            withTimeout(5000) { while(manager.activeNotifications.singleOrNull { it.id==100 }?.notification?.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()!=english) delay(50) }
            rule.activityRule.scenario.recreate()
            val settings=rule.onNodeWithText("Settings",substring=false)
            if(rule.activity.resources.configuration.screenWidthDp>=720) settings.performScrollTo()
            settings.performClick()
            rule.onNode(hasText("Language:",substring=true)).performScrollTo().performClick()
            val french=Locale.forLanguageTag("fr-FR")
            rule.onNodeWithText(french.getDisplayName(french),substring=false).performScrollTo().performClick()
            rule.waitUntil(10000) { AppLocales.selected(app)=="fr-FR" }
            val expected=AppLocales.wrap(app).resources.getQuantityString(R.plurals.reminder_count,count,count)
            assertNotEquals(english,expected)
            withTimeout(7000) { while(manager.activeNotifications.singleOrNull { it.id==100 }?.notification?.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()!=expected) delay(50) }
        } finally {
            app.database.dao().delete(plant);AppLocales.choose(app,old);app.settings.set("reminders",enabled);Reminders.refresh(app)
        }
    }
}
