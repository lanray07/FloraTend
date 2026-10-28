package com.floratend.app

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import java.util.Locale

object AppLocales {
    val supported = listOf("en-GB","en-US","en-AU","es-ES","es-419","fr-FR","fr-CA","de-DE","it-IT","pt-BR","pt-PT","nl-NL","pl-PL","sv-SE","ja-JP","hi-IN","ar","tr-TR","id-ID")
    fun selected(context: Context): String = context.getSharedPreferences("language",Context.MODE_PRIVATE).getString("tag", "").orEmpty()
    fun current(context: Context): Locale = selected(context).takeIf { it in supported }?.let(Locale::forLanguageTag)
        ?: Resources.getSystem().configuration.locales[0]
    fun choose(context: Context, tag: String) {
        require(tag.isEmpty() || tag in supported)
        context.getSharedPreferences("language",Context.MODE_PRIVATE).edit().putString("tag",tag).apply()
    }
    fun wrap(context: Context): Context {
        val locale=current(context)
        val config=Configuration(context.resources.configuration)
        config.setLocales(LocaleList(locale));config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }
    fun dictionaryTag(locale: Locale): String? {
        val exact=locale.toLanguageTag()
        return supported.firstOrNull { it.equals(exact,true) }
            ?: supported.firstOrNull { Locale.forLanguageTag(it).language==locale.language }
    }
}
