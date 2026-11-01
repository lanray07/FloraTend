package com.floratend.app

import java.text.Collator
import java.util.Locale

/** Persistent category keys remain stable; search also accepts translated labels. */
object PlantLibraryQuery {
    fun apply(plants:List<Plant>, query:String, category:String, descending:Boolean,
              categoryLabels:Map<String,String>, locale:Locale = Locale.getDefault()):List<Plant> {
        val needle=query.trim().lowercase(locale)
        val collator=Collator.getInstance(locale)
        val matches=plants.filter { plant ->
            (category.isEmpty() || plant.category==category) &&
                listOf(plant.name,plant.species,plant.location,plant.category,categoryLabels[plant.category].orEmpty())
                    .any { it.lowercase(locale).contains(needle) }
        }.sortedWith { left,right ->
            collator.compare(left.name,right.name).takeIf { it!=0 } ?: left.id.compareTo(right.id)
        }
        return if(descending) matches.reversed() else matches
    }
}
