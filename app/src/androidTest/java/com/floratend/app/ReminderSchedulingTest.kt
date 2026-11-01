package com.floratend.app

import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import org.junit.Assert.*
import org.junit.Test

/** Actual WorkManager state, no battery-percentage estimate or forced idle claim. */
class ReminderSchedulingTest {
    @Test fun disabledStateRemainsWithoutWorkAfterProcessRestart() = runBlocking {
        val app=InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as FloraApp
        check(BuildConfig.APPLICATION_ID.endsWith(".perf") || BuildConfig.APPLICATION_ID.endsWith(".qa"))
        assertFalse("Invoke separately after disabling reminders and a real process stop",app.settings.reminders.first())
        val work=WorkManager.getInstance(app)
        withTimeout(10000) {
            while(listOf("care-checks","care-refresh").any { name -> work.getWorkInfosForUniqueWork(name).get().any { !it.state.isFinished } }) delay(50)
        }
        repeat(4) {
            delay(250)
            assertTrue(listOf("care-checks","care-refresh").all { name -> work.getWorkInfosForUniqueWork(name).get().all { it.state.isFinished } })
        }
    }
    @Test fun disabledRemindersDoNotKeepPeriodicOrRefreshWork() = runBlocking {
        val app=InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as FloraApp
        check(BuildConfig.APPLICATION_ID.endsWith(".perf") || BuildConfig.APPLICATION_ID.endsWith(".qa"))
        val original=app.settings.reminders.first()
        val work=WorkManager.getInstance(app)
        fun active(name:String)=work.getWorkInfosForUniqueWork(name).get().filter { !it.state.isFinished }
        suspend fun await(stage:String,assertion:()->Boolean) {
            try { withTimeout(10000) { while(!assertion()) delay(50) } }
            catch (cause:TimeoutCancellationException) {
                throw AssertionError("$stage: periodic=${active("care-checks").map { it.state }} refresh=${active("care-refresh").map { it.state }}",cause)
            }
        }
        try {
            app.settings.set("reminders",true)
            await("Enabled reminders have one periodic job") { active("care-checks").size==1 }
            repeat(3) { Reminders.refresh(app) }
            assertEquals(1,active("care-checks").size)
            app.settings.set("reminders",false)
            await("Disabled reminders cancel both jobs") { active("care-checks").isEmpty() && active("care-refresh").isEmpty() }
            repeat(3) { Reminders.refresh(app) }
            delay(500)
            assertTrue(active("care-checks").isEmpty())
            assertTrue(active("care-refresh").isEmpty())
            app.settings.set("reminders",true)
            await("Re-enabled reminders have one periodic job") { active("care-checks").size==1 }
            val enabledJobs=active("care-checks")
            assertEquals(1,enabledJobs.size)
            assertEquals(43_200_000L,enabledJobs.single().periodicityInfo!!.repeatIntervalMillis)
        } finally {
            app.settings.set("reminders",original)
        }
    }
}
