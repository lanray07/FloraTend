package com.floratend.app

import android.graphics.Bitmap
import android.net.Uri
import android.os.Debug
import android.os.Handler
import android.os.HandlerThread
import android.view.FrameMetrics
import android.view.Window
import androidx.compose.ui.test.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import java.io.File
import coil.Coil
import coil.EventListener
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.Options
import coil.decode.Decoder
import coil.decode.DecodeResult
import android.os.Looper
import org.json.JSONArray
import java.util.Collections

class PhotoCollectionTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val app get()=rule.activity.application as FloraApp
    private val prefix="qa-large-photo-"
    private val paths=mutableListOf<File>()
    private val frames=Collections.synchronizedList(mutableListOf<Long>())
    private val metricsThread=HandlerThread("qa-frame-metrics")
    private var originalLoader:ImageLoader?=null
    private val decodes=Collections.synchronizedList(mutableListOf<JSONObject>())
    private val frameDetails=Collections.synchronizedList(mutableListOf<JSONObject>())
    private var listener:Window.OnFrameMetricsAvailableListener?=null
    @After fun cleanup():Unit=runBlocking {
        if(InstrumentationRegistry.getArguments().getString("standalone")=="seed") return@runBlocking
        listener?.let { callback -> rule.activity.window.removeOnFrameMetricsAvailableListener(callback) }
        metricsThread.quitSafely()
        originalLoader?.let { Coil.setImageLoader(it) }
        app.database.dao().plants().first().filter { it.id.startsWith(prefix) }.forEach { app.database.dao().delete(it) }
        paths.forEach { it.delete() }
    }
    @Test fun savedPhotoCollectionScrollsWithoutDecodeOrMemoryFailure() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa") || BuildConfig.APPLICATION_ID.endsWith(".perf"))
        AppLocales.choose(app,"en-GB")
        runBlocking { app.settings.set("onboarded",true) }
        val variant=InstrumentationRegistry.getArguments().getString("photoVariant") ?: "photos"
        val loaderMode=InstrumentationRegistry.getArguments().getString("loaderMode") ?: "default"
        originalLoader=Coil.imageLoader(app)
        val builder=originalLoader!!.newBuilder().eventListenerFactory {
            object:EventListener {
                private var start=0L
                override fun decodeStart(request:ImageRequest,decoder:Decoder,options:Options) { start=System.nanoTime() }
                override fun decodeEnd(request:ImageRequest,decoder:Decoder,options:Options,result:DecodeResult?) {
                    if(result==null) return
                    decodes.add(JSONObject().put("millis",(System.nanoTime()-start)/1e6)
                        .put("width",result.drawable.intrinsicWidth).put("height",result.drawable.intrinsicHeight)
                        .put("onMain",Looper.myLooper()==Looper.getMainLooper())
                        .put("bitmapConfig",(result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap?.config?.name))
                }
            }
        }
        if(loaderMode=="software") builder.allowHardware(false)
        if(loaderMode=="serial") builder.bitmapFactoryMaxParallelism(1)
        if(loaderMode=="worker") builder.interceptorDispatcher(kotlinx.coroutines.Dispatchers.Default)
        Coil.setImageLoader(builder.build())
        val original=File(app.cacheDir,"qa-photo-collection-source.jpg")
        val image=Bitmap.createBitmap(3200,2400,Bitmap.Config.ARGB_8888).apply { eraseColor(android.graphics.Color.rgb(40,130,65)) }
        original.outputStream().use { image.compress(Bitmap.CompressFormat.JPEG,90,it) };image.recycle()
        try {
            runBlocking {
                repeat(120) { index ->
                    val path=Photos.import(app,Uri.fromFile(original));paths+=File(path)
                    app.database.dao().save(Plant(id=prefix+index,name="Photo fern ${index.toString().padStart(3,'0')}",photo=if(variant=="no-photos") null else path))
                }
            }
        } finally { original.delete() }
        if(InstrumentationRegistry.getArguments().getString("standalone")=="seed") return
        rule.activityRule.scenario.recreate()
        rule.waitUntil(15000) { rule.onAllNodesWithTag("plant-grid").fetchSemanticsNodes().isNotEmpty() }
        metricsThread.start()
        listener=Window.OnFrameMetricsAvailableListener { _,metrics,_ ->
            frames.add(metrics.getMetric(FrameMetrics.TOTAL_DURATION))
            val row=JSONObject()
            for((name,key) in listOf("unknown" to FrameMetrics.UNKNOWN_DELAY_DURATION,"input" to FrameMetrics.INPUT_HANDLING_DURATION,"animation" to FrameMetrics.ANIMATION_DURATION,"layout" to FrameMetrics.LAYOUT_MEASURE_DURATION,"draw" to FrameMetrics.DRAW_DURATION,"sync" to FrameMetrics.SYNC_DURATION,"commands" to FrameMetrics.COMMAND_ISSUE_DURATION,"swap" to FrameMetrics.SWAP_BUFFERS_DURATION)) row.put(name,metrics.getMetric(key)/1e6)
            frameDetails.add(row)
        }
        rule.activityRule.scenario.onActivity { it.window.addOnFrameMetricsAvailableListener(listener!!,Handler(metricsThread.looper)) }
        val before=Debug.getPss()
        val start=System.nanoTime()
        val grid=rule.onNodeWithTag("plant-grid")
        repeat(16) { grid.performTouchInput { swipeUp(durationMillis=350) };rule.waitForIdle() }
        grid.performScrollToIndex(119)
        rule.onNodeWithText("Photo fern 119").assertIsDisplayed()
        grid.performScrollToIndex(0)
        rule.onNodeWithText("Photo fern 000").assertIsDisplayed()
        val sorted=synchronized(frames) { frames.sorted() }
        val report=JSONObject().put("fixturePhotoCount",paths.size).put("variant",variant).put("debuggable",BuildConfig.DEBUG).put("loaderMode",loaderMode).put("decodes",JSONArray(decodes.toList())).put("frameDetails",JSONArray(frameDetails.toList())).put("sourceDimensions","3200x2400")
            .put("privateCopyMaxEdge",1600).put("allPrivateFilesPresent",paths.all { it.exists() && it.length()>0 })
            .put("pssBeforeKiB",before).put("pssAfterKiB",Debug.getPss())
            .put("scrollTestSeconds",(System.nanoTime()-start)/1e9).put("observedFrameCount",sorted.size)
            .put("frameP95Millis",if(sorted.isEmpty()) JSONObject.NULL else sorted[(sorted.size*0.95).toInt().coerceAtMost(sorted.lastIndex)]/1e6)
            .put("scope","Physical HD10 API28 QA fixture collection; instrumentation and image setup excluded from startup claims.")
        val folder=File(app.getExternalFilesDir(null),"performance").apply { mkdirs() }
        File(folder,"photo-collection-$variant-$loaderMode.json").writeText(report.toString(2))
        assertTrue(paths.all { it.exists() && it.length()>0 })
        assertTrue("Real rendered frame observations",sorted.isNotEmpty())
    }
}
