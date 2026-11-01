package com.floratend.app

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.io.File

/** Synthetic native UI contracts, never evidence of live Pl@ntNet recognition. */
class PlantIdentificationContractTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val app get()=rule.activity.application as FloraApp
    private var photo:String?=null
    @Before fun prepare() {check(BuildConfig.APPLICATION_ID.endsWith(".qa"))}
    private fun fixture(value:PlantIdentifier?) { app.javaClass.getMethod("setIdentificationFixture",PlantIdentifier::class.java).invoke(app,value) }
    @After fun clean() {if(BuildConfig.APPLICATION_ID.endsWith(".qa"))fixture(null);photo?.let { File(it).delete() }}
    @Test fun cancelConsentDoesNotUploadAndChoiceRemainsEditableUntilSave() {
        val response=CompletableDeferred<IdentificationResult>()
        var calls=0;var saved:Plant?=null
        fixture(object:PlantIdentifier {
            override val available=true
            override suspend fun identify(photo:String,language:String):IdentificationResult {calls++;return response.await()}
        })
        val bitmap=Bitmap.createBitmap(40,40,Bitmap.Config.ARGB_8888)
        photo=Photos.save(app,bitmap);bitmap.recycle()
        val plant=Plant(name="QA identification",photo=photo)
        rule.activity.runOnUiThread {rule.activity.setContent {MaterialTheme {PlantEditor(plant,{}, {saved=it})}}}
        rule.waitForIdle()
        fun click(id:Int)=rule.onNodeWithText(rule.activity.getString(id)).performClick()
        rule.onNodeWithText(rule.activity.getString(R.string.identify_photo)).performScrollTo();click(R.string.identify_photo)
        rule.onNodeWithText(rule.activity.getString(R.string.identification_consent)).assertExists()
        rule.onAllNodesWithText(rule.activity.getString(R.string.cancel)).onLast().performClick()
        rule.runOnIdle {assertEquals(0,calls);assertNull(saved)}
        rule.onNodeWithText(rule.activity.getString(R.string.identify_photo)).performScrollTo();click(R.string.identify_photo);click(R.string.send_photo)
        rule.waitUntil(5000) {calls==1}
        rule.runOnIdle {response.complete(IdentificationResult.Suggestions(listOf(SpeciesSuggestion("Ajuga genevensis",emptyList(),.9))))}
        rule.waitUntil(5000) {rule.onAllNodesWithText("Ajuga genevensis").fetchSemanticsNodes().isNotEmpty()}
        rule.onNodeWithText("Ajuga genevensis").performScrollTo().performClick()
        rule.onNodeWithText(rule.activity.getString(R.string.species)).performScrollTo().performTextReplacement("Corrected species")
        rule.runOnIdle {assertNull(saved)}
        click(R.string.save)
        rule.runOnIdle {assertEquals("Corrected species",saved?.species);assertEquals(1,calls)}
    }
    @Test fun privatePhotoCannotReadOtherAppFilesOrOversizedInputs() {
        val outside=File(app.cacheDir,"identification-outside.jpg").apply {writeBytes(byteArrayOf(-1,-40,-1,-39))}
        val oversized=File(app.filesDir,"photos/qa-identification-too-large.jpg").apply {parentFile?.mkdirs();outputStream().use { it.write(ByteArray(2*1024*1024+1)) }}
        try {
            assertThrows(IllegalArgumentException::class.java) {ServerPlantIdentifier.privatePhoto(app,outside.path)}
            assertThrows(IllegalArgumentException::class.java) {ServerPlantIdentifier.privatePhoto(app,oversized.path)}
        } finally {outside.delete();oversized.delete()}
    }
    @Test fun androidPlatformJsonParsesAndRejectsUnsafeServerResults() {
        val json="""{"provider":"plantnet","state":"identified","language":"en","results":[{"scientificName":"Ajuga genevensis","commonNames":["Blue bugle"],"score":0.9}]}"""
        assertEquals("Ajuga genevensis",(parseIdentification(json) as IdentificationResult.Suggestions).items.single().scientificName)
        assertThrows(IllegalArgumentException::class.java) {parseIdentification(json.replace("0.9","\"0.9\""))}
        assertThrows(IllegalArgumentException::class.java) {parseIdentification(json.replace("Ajuga genevensis","https://bad.example"))}
    }
}
