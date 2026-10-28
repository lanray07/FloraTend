package com.floratend.app

import android.os.Bundle
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SpeechCapabilityTest {
    @Test fun inspectInstalledSpeechCapabilitiesAndSynthesizeWhenSupported() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val context=instrumentation.targetContext
        val initialized=CountDownLatch(1)
        var engine:TextToSpeech?=null
        var initStatus=TextToSpeech.ERROR
        instrumentation.runOnMainSync {
            engine=TextToSpeech(context) { status -> initStatus=status;initialized.countDown() }
        }
        val report=JSONObject().put("recognitionAvailable",SpeechRecognizer.isRecognitionAvailable(context))
        val audio=File(context.cacheDir,"acceptance-speech.wav")
        try {
            val initFinished=initialized.await(15,TimeUnit.SECONDS)
            report.put("ttsInitializationCompleted",initFinished).put("ttsInitStatus",initStatus)
            if(initFinished && initStatus==TextToSpeech.SUCCESS) {
                val tts=engine!!
                report.put("defaultTtsEngine",tts.defaultEngine)
                val locales=listOf("en-GB","en-US","en-AU","es-ES","es-419","fr-FR","fr-CA","de-DE","it-IT","pt-BR","pt-PT","nl-NL","pl-PL","sv-SE","ja-JP","hi-IN","ar","tr-TR","id-ID")
                report.put("ttsLocaleCapabilities",JSONArray().apply { locales.forEach { put(JSONObject().put("locale",it).put("availabilityCode",tts.isLanguageAvailable(Locale.forLanguageTag(it)))) } })
                val languageStatus=tts.setLanguage(Locale.getDefault())
                report.put("currentLanguageStatus",languageStatus)
                if(languageStatus>=0) {
                    val finished=CountDownLatch(1)
                    var succeeded=false
                    tts.setOnUtteranceProgressListener(object:UtteranceProgressListener() {
                        override fun onStart(id:String?) {}
                        override fun onDone(id:String?) { succeeded=true;finished.countDown() }
                        @Deprecated("Required legacy callback") override fun onError(id:String?) { finished.countDown() }
                    })
                    val accepted=tts.synthesizeToFile("FloraTend care reminders are ready.",Bundle(),audio,"acceptance-tts")
                    val done=finished.await(20,TimeUnit.SECONDS)
                    report.put("synthesisAcceptedCode",accepted).put("synthesisCompleted",done).put("synthesisSucceeded",succeeded).put("synthesizedBytes",audio.length())
                    assertEquals("Available TTS engine must accept synthesis",TextToSpeech.SUCCESS,accepted)
                    assertTrue("Available voice must produce an audio file",done && succeeded && audio.length()>44)
                } else report.put("synthesis","Unavailable for current locale; app retains text feedback")
            } else report.put("synthesis","No initialized TTS engine; app retains text feedback")
        } finally {
            val folder=File(context.getExternalFilesDir(null),"acceptance").apply { mkdirs() }
            File(folder,"speech-engine-test.json").writeText(report.toString(2))
            engine?.stop();engine?.shutdown();audio.delete()
        }
    }
}
