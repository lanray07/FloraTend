package com.floratend.app

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class PlantLibraryQueryTest {
    @Test fun translatedCategorySearchKeepsCanonicalKeysAndFilters() {
        val herb=Plant(id="herb",name="Basil",category="Herb")
        val indoor=Plant(id="indoor",name="Fern",category="Indoor")
        val all=listOf(herb,indoor)
        for(label in listOf("Kräuter","허브","香草植物")) {
            assertEquals(listOf(herb),PlantLibraryQuery.apply(all,label,"",false,mapOf("Herb" to label)))
            assertEquals(emptyList<Plant>(),PlantLibraryQuery.apply(all,label,"Indoor",false,mapOf("Herb" to label)))
        }
        assertEquals(listOf(herb),PlantLibraryQuery.apply(all,"Herb","",false,emptyMap()))
    }
    @Test fun sortingUsesLocaleAndDescendingReversesTheSameOrder() {
        val plants=listOf(Plant(id="a",name="ä"),Plant(id="z",name="z"),Plant(id="b",name="a"))
        val result=PlantLibraryQuery.apply(plants,"","",false,emptyMap(),Locale.forLanguageTag("sv-SE"))
        assertEquals(listOf("a","z","ä"),result.map { it.name })
        assertEquals(result.reversed(),PlantLibraryQuery.apply(plants,"","",true,emptyMap(),Locale.forLanguageTag("sv-SE")))
    }
    @Test fun turkishSearchUsesLocaleCasing() {
        val plant=Plant(name="Isparta",location="Window")
        assertEquals(listOf(plant),PlantLibraryQuery.apply(listOf(plant),"ısparta","",false,emptyMap(),Locale.forLanguageTag("tr-TR")))
    }
}
