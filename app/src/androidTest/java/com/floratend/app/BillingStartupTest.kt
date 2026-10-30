package com.floratend.app

import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test

/** Controlled cache fixtures, not genuine Amazon receipts or live-purchase evidence. */
class BillingStartupTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val app get() = instrumentation.targetContext.applicationContext as FloraApp
    private val cache get() = app.getSharedPreferences("qa_startup_entitlement", Context.MODE_PRIVATE)
    private val settings get() = FloraSettings(isolatedContext)
    private var previousPremium = false
    private val isolatedContext get() = object : ContextWrapper(app) {
        override fun getApplicationContext(): Context = this
        // Missing public key prevents this test instance from contacting Amazon.
        override fun getAssets(): AssetManager = instrumentation.context.assets
        override fun getSharedPreferences(name: String, mode: Int) = cache
    }
    @Before fun rememberSettings() = runBlocking { previousPremium = app.settings.premiumProjection.first() }
    @After fun restoreSettings(): Unit = runBlocking {
        cache.edit().clear().commit()
        app.settings.set("premium", previousPremium)
    }
    private fun fixture(active: Boolean) {
        check(cache.edit().clear().putString("mode", "PRODUCTION")
            .putString("owner", "controlled-fixture-owner")
            .putString("receipt", "controlled-fixture-receipt")
            .putBoolean("active", active).commit())
    }
    @Test fun restartRecoversGrantWhenCacheWasWrittenBeforeProjection(): Unit = runBlocking {
        fixture(true)
        settings.set("premium", false)
        AmazonBilling(isolatedContext, settings).register()
        withTimeout(3000) { settings.premiumProjection.first { it } }
        withTimeout(3000) { settings.premium.first { it } }
    }
    @Test fun restartRecoversRevocationWhenCacheWasWrittenBeforeProjection(): Unit = runBlocking {
        fixture(false)
        settings.set("premium", true)
        AmazonBilling(isolatedContext, settings).register()
        withTimeout(3000) { settings.premiumProjection.first { !it } }
        withTimeout(3000) { settings.premium.first { !it } }
    }
}
