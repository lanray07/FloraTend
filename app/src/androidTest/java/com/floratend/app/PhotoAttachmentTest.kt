package com.floratend.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.*
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.io.File

/** Controlled picker return URI, real ContentResolver decoding and real editor Save. */
class PhotoAttachmentTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val app get()=rule.activity.application as FloraApp
    private val id="qa-photo-contract"
    private val copied=mutableListOf<File>()
    private lateinit var source:File
    private var launches=0
    private var initialPaths=emptySet<String>()
    @Before fun prepare() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa"))
        AppLocales.choose(app,"en-GB")
        initialPaths=File(app.filesDir,"photos").listFiles().orEmpty().map { it.path }.toSet()
        source=File(app.cacheDir,"qa-photos/source.jpg").apply { parentFile!!.mkdirs() }
        val image=Bitmap.createBitmap(3200,2400,Bitmap.Config.ARGB_8888).apply { eraseColor(android.graphics.Color.GREEN) }
        source.outputStream().use { assertTrue(image.compress(Bitmap.CompressFormat.JPEG,90,it)) };image.recycle()
    }
    @After fun cleanup(): Unit=runBlocking {
        app.database.dao().plant(id)?.let { app.database.dao().delete(it) }
        copied.forEach { it.delete() };source.delete();Unit
    }
    private fun owner()=object:ActivityResultRegistryOwner {
        override val activityResultRegistry=object:ActivityResultRegistry() {
            override fun <I,O> onLaunch(code:Int,contract:ActivityResultContract<I,O>,input:I,options:ActivityOptionsCompat?) {
                assertEquals("image/*",input)
                launches++
                val uri=FileProvider.getUriForFile(app,BuildConfig.APPLICATION_ID+".qa.fixtures",source)
                Handler(Looper.getMainLooper()).post { dispatchResult(code,uri) }
            }
        }
    }
    private fun verify(path:String?) {
        assertEquals(1,launches)
        val saved=File(requireNotNull(path));copied+=saved
        assertTrue(saved.canonicalPath.startsWith(File(app.filesDir,"photos").canonicalPath+File.separator))
        source.delete() // The selected provider file is no longer present.
        val bitmap=BitmapFactory.decodeFile(saved.path)
        assertNotNull(bitmap);assertEquals(1600,bitmap.width);assertEquals(1200,bitmap.height)
        assertTrue(android.graphics.Color.green(bitmap.getPixel(0,0))>240);bitmap.recycle()
    }
    @Test fun plantEditorImportsPrivateDownsampledCopy() {
        var saved:Plant?=null
        val owner=owner()
        rule.activityRule.scenario.onActivity { activity -> activity.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner) {
                MaterialTheme { PlantEditor(Plant(id=id,name="Photo fern"),{}, { saved=it }) }
            }
        } }
        rule.onNodeWithText("Choose photo").performScrollTo().performClick()
        rule.waitUntil(10000) { File(app.filesDir,"photos").listFiles().orEmpty().any { it.path !in initialPaths } }
        rule.waitForIdle()
        rule.waitUntil(10000) {
            rule.onAllNodesWithContentDescription(rule.activity.getString(R.string.plant_photo)).fetchSemanticsNodes().isNotEmpty() &&
                rule.onAllNodes(hasText("Save",substring=false) and isEnabled()).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Save",substring=false).performClick()
        rule.waitUntil(10000) { saved?.photo!=null }
        runBlocking { app.database.dao().save(requireNotNull(saved)) }
        verify(runBlocking { app.database.dao().plant(id)!!.photo })
    }
    @Test fun journalEditorAttachesSelectedPhotograph() {
        var saved:JournalEntry?=null
        val plant=Plant(id=id,name="Journal photo fern")
        runBlocking { app.database.dao().save(plant) }
        val owner=owner()
        rule.activityRule.scenario.onActivity { activity -> activity.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner) {
                MaterialTheme { JournalEditor(listOf(plant),null,{}, { saved=it },{}) }
            }
        } }
        rule.onNodeWithText("Choose photo").performScrollTo().performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithText("Save",substring=false).fetchSemanticsNodes().isNotEmpty() }
        rule.waitUntil(10000) { runCatching { rule.onNodeWithText("Save",substring=false).assertIsEnabled();true }.getOrDefault(false) }
        rule.waitUntil(10000) {
            rule.onAllNodesWithContentDescription(rule.activity.getString(R.string.plant_photo)).fetchSemanticsNodes().isNotEmpty() &&
                rule.onAllNodes(hasText("Save",substring=false) and isEnabled()).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Save",substring=false).performClick()
        rule.waitUntil(10000) { saved?.photo!=null }
        runBlocking { app.database.dao().save(requireNotNull(saved)) }
        verify(requireNotNull(saved).photo)
    }
}
