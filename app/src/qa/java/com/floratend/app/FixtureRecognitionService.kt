package com.floratend.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionService
import android.speech.SpeechRecognizer

/** Android binder contract fixture. Never opens a microphone or recognises audio. */
class FixtureRecognitionService : RecognitionService() {
    private val handler=Handler(Looper.getMainLooper())
    override fun onStartListening(intent:Intent,listener:Callback) {
        val prefs=getSharedPreferences("qa_speech_fixture",MODE_PRIVATE)
        prefs.edit().putInt("starts",prefs.getInt("starts",0)+1).apply()
        listener.readyForSpeech(Bundle())
        when(prefs.getString("scenario","result")) {
            "error" -> handler.postDelayed({ listener.error(SpeechRecognizer.ERROR_NETWORK) },1500)
            "pending" -> Unit
            else -> handler.postDelayed({
                listener.endOfSpeech()
                listener.results(Bundle().apply { putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION,arrayListOf("Add a new plant.")) })
            },1800)
        }
    }
    override fun onStopListening(listener:Callback) { handler.removeCallbacksAndMessages(null);listener.error(SpeechRecognizer.ERROR_CLIENT) }
    override fun onCancel(listener:Callback) {
        handler.removeCallbacksAndMessages(null)
        val prefs=getSharedPreferences("qa_speech_fixture",MODE_PRIVATE)
        prefs.edit().putInt("cancellations",prefs.getInt("cancellations",0)+1).apply()
    }
}
