package com.floratend.app

import android.content.Intent
import android.os.Debug
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.json.JSONObject
import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import java.io.File

/** Sampling diagnostics only. Never targets Amazon's package or certifies frame timings. */
class MethodSamplingTest {
    @Test fun sampleOnlyControlledPerfGardenAndTypedVoice() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val context=instrumentation.targetContext
        assumeTrue("Disposable perf package only",context.packageName=="com.floratend.app.perf")
        val device=UiDevice.getInstance(instrumentation)
        val folder=File(context.getExternalFilesDir(null),"method-profiles").apply { mkdirs() }
        context.startActivity(requireNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        assertTrue(device.wait(Until.hasObject(By.textContains("120 plants")),15000))
        assertTrue(device.wait(Until.hasObject(By.text("Photo fern 000")),10000))
        fun sample(name:String,actions:()->Unit) {
            val output=File(folder,"$name.trace")
            Debug.startMethodTracingSampling(output.absolutePath,32*1024*1024,1000)
            try { actions();SystemClock.sleep(500) } finally { Debug.stopMethodTracing() }
            assertTrue("Sampling output must exist",output.isFile && output.length()>0)
        }
        sample("garden") {
            repeat(8) { device.executeShellCommand("input swipe 1550 1000 1550 650 350") }
        }
        assertFalse("The controlled grid must actually scroll",device.hasObject(By.text("Photo fern 000")))
        device.click(78,485)
        assertTrue(device.wait(Until.hasObject(By.text("A helping voice")),10000))
        SystemClock.sleep(1000)
        sample("voice") {
            device.click(1000,550);SystemClock.sleep(500)
            device.executeShellCommand("input text Read%smy%sreminders")
            SystemClock.sleep(500);device.pressBack()
        }
        assertTrue(device.wait(Until.hasObject(By.textContains("Read my reminders")),5000))
        File(folder,"scope.json").writeText(JSONObject().put("package",context.packageName)
            .put("controlledPlants",120).put("instrumentationAttached",true).put("samplingIntervalUs",1000)
            .put("releaseFrameGateApproved",false).put("microphoneActivated",false).put("purchaseAttempted",false)
            .put("rawProfilesMustRemainPrivate",true).toString(2))
    }
}
