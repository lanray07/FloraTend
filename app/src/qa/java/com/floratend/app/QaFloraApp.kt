package com.floratend.app

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.speech.SpeechRecognizer

class QaFloraApp : FloraApp() {
    // Test-only transport seam. Absent from production/Amazon APKs.
    var identificationFixture:PlantIdentifier?=null
    override val plantIdentifier:PlantIdentifier get()=identificationFixture ?: super.plantIdentifier
    override fun createSpeechRecognizer(context: Context): SpeechRecognizer {
        val component=ComponentName(this,FixtureRecognitionService::class.java)
        return if(packageManager.getComponentEnabledSetting(component)==PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
            SpeechRecognizer.createSpeechRecognizer(context,component)
        else super.createSpeechRecognizer(context)
    }
}
