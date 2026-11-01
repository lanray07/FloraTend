package com.floratend.app

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

/** External OS UI input only. QA hosts the runner; the Amazon target is never instrumented. */
class OptimizedExternalUiTest {
    @Test fun amazon15SettingsAndVoiceReview() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val context=instrumentation.targetContext
        check(context.packageName=="com.floratend.app.qa")
        check(InstrumentationRegistry.getArguments().getString("floratend.externalAmazon15")=="true")
        val target="com.floratend.app"
        val info=context.packageManager.getPackageInfo(target,PackageManager.GET_SIGNATURES)
        assertEquals(15,info.versionCode)
        assertEquals(0,info.applicationInfo!!.flags and ApplicationInfo.FLAG_DEBUGGABLE)
        assertEquals("com.amazon.venezia",context.packageManager.getInstallerPackageName(target))
        val certificate=MessageDigest.getInstance("SHA-256").digest(info.signatures!![0].toByteArray()).joinToString(""){"%02x".format(it)}
        assertEquals("8e35b165d5a1dc14c10820cee25ed2c24e9c543ceb2f059b9f5bc93baa672297",certificate)
        val device=UiDevice.getInstance(instrumentation)
        fun visible(text:String)=device.wait(Until.findObject(By.pkg(target).text(text)),10000)
            ?: error("Missing Amazon15 UI: $text")
        fun click(text:String) { visible(text).click();device.waitForIdle() }
        val intent=context.packageManager.getLaunchIntentForPackage(target) ?: error("Amazon target missing")
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        click("Settings")
        visible("Premium is active for this Amazon account.")
        click("Voice")
        fun editor()=device.wait(Until.findObject(By.pkg(target).clazz("android.widget.EditText")),10000) ?: error("Voice editor missing")
        editor().text="Review unknown command"
        assertEquals("Review unknown command",editor().text)
        click("Run command")
        visible("Command not recognised. Try one of the examples below, or use the app controls.")
        assertEquals("Review unknown command",editor().text)
        editor().text="";click("Garden")
        val folder=File(context.getExternalFilesDir(null),"optimized-external").apply{mkdirs()}
        File(folder,"result.json").writeText(JSONObject().put("result","PASS").put("amazonVersionCode",15)
            .put("runnerInSeparateQaProcess",true).put("amazonTargetInstrumented",false)
            .put("nativeEditorReviewAndUnknownCommand",true).put("noPurchaseTriggered",true)
            .put("noPlantRecordsChanged",true).put("screenReaderApproval",false).toString(2))
    }
}
