package com.floratend.app

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PlantIdentificationTest {
    @Test fun languageMappingPreservesSelectedLanguageAndRejectsUnverifiedScriptFallback() {
        assertEquals("ja",identificationLanguage("ja-JP"));assertEquals("fr",identificationLanguage("fr-CA"));assertEquals("en",identificationLanguage("invalid"))
        for(tag in listOf("zh-Hant","zh-Hant-TW","zh-TW","zh-HK","zh-MO"))assertFalse(tag,identificationCommonNamesAllowed(tag))
        for(tag in listOf("zh-Hans","zh-CN","ja-JP","en-GB"))assertTrue(tag,identificationCommonNamesAllowed(tag))
    }
    @Test fun onlyConfiguredHttpsEndpointCanEnableUploads() {
        assertTrue(configuredIdentifier("https://identifier.example/identify"))
        listOf("","http://identifier.example/identify","https://user@identifier.example/identify","https://identifier.example/identify?key=x","https://identifier.example/identify#x","https://identifier.example/other").forEach { assertFalse(it,configuredIdentifier(it)) }
    }
    @Test fun boundedReaderRejectsOversizedBodies() {
        assertArrayEquals(byteArrayOf(1,2),byteArrayOf(1,2).inputStream().readBounded(2))
        assertThrows(IllegalArgumentException::class.java) { ByteArray(3).inputStream().readBounded(2) }
    }
    @Test fun actualJsonParserKeepsOnlyValidatedSuggestions() {
        val json="""{"state":"identified","provider":"plantnet","results":[{"scientificName":"Ajuga genevensis","commonNames":["Blue bugle"],"score":0.9}]}"""
        val parsed=parseIdentification(json) as IdentificationResult.Suggestions
        assertEquals("Ajuga genevensis",parsed.items.single().scientificName)
        assertEquals(.9,parsed.items.single().score,0.0)
        for(value in listOf(json.replace("0.9","2"),json.replace("0.9","\"0.9\""),json.replace("Ajuga genevensis","https://bad.example"),json.replace("plantnet","other"),json.replace("commonNames\":[\"Blue bugle\"]","commonNames\":[\"bad\\u202ename\"]"))) {
            assertThrows(Exception::class.java) { parseIdentification(value) }
        }
        assertEquals(IdentificationResult.NoMatch,parseIdentification("""{"provider":"plantnet","state":"no_match","results":[]}"""))
    }
    @Test fun noUploadBeforeSendAndPhotoChangeRejectsLateResponse()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val response=CompletableDeferred<IdentificationResult>();var calls=0
            val gateway=object:PlantIdentifier {
                override val available=true
                override suspend fun identify(photo:String,language:String):IdentificationResult {calls++;return withContext(NonCancellable) {response.await()} }
            }
            val model=IdentificationViewModel(gateway)
            model.bind("one");runCurrent();assertEquals(0,calls)
            model.send("one","fr");model.send("one","fr");runCurrent();assertEquals(1,calls);assertTrue(model.state.value.loading)
            model.bind("two");response.complete(IdentificationResult.NoMatch);runCurrent()
            assertEquals(IdentificationState(),model.state.value)
            model.send("one","fr");runCurrent();assertEquals(1,calls)
        } finally {Dispatchers.resetMain()}
    }
    @Test fun cancelClearsResultsAndUnavailableConfigurationCannotSend()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var calls=0
            val gateway=object:PlantIdentifier {override val available=false;override suspend fun identify(photo:String,language:String):IdentificationResult {calls++;return IdentificationResult.NoMatch}}
            val model=IdentificationViewModel(gateway);model.bind("one");model.send("one","en");runCurrent()
            assertEquals(0,calls);model.cancel();assertEquals(IdentificationState(),model.state.value)
        } finally {Dispatchers.resetMain()}
    }
    @Test fun selectionRequiresCurrentPhotoAndActualReturnedName()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val gateway=object:PlantIdentifier {override val available=true;override suspend fun identify(photo:String,language:String)=IdentificationResult.Suggestions(listOf(SpeciesSuggestion("Ajuga",emptyList(),.9)))}
            val model=IdentificationViewModel(gateway);model.bind("one");model.send("one","en");runCurrent()
            assertEquals("Ajuga",model.selectedSpecies("one","Ajuga"))
            assertNull(model.selectedSpecies("two","Ajuga"));assertNull(model.selectedSpecies("one","Other"))
            model.cancel();assertNull(model.selectedSpecies("one","Ajuga"))
        } finally {Dispatchers.resetMain()}
    }
}
