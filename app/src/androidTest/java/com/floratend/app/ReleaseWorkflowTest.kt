package com.floratend.app

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.io.File

/** Additional real UI workflows in the disposable .qa package, never store billing. */
class ReleaseWorkflowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val app get() = rule.activity.application as FloraApp
    private val prefix = "release-workflow-"
    private val photos = mutableListOf<File>()
    @Before fun prepare() {
        check(BuildConfig.APPLICATION_ID.endsWith(".qa")) { "Disposable QA package required" }
        runBlocking {
            app.settings.set("onboarded", true)
            app.settings.set("imperial", false)
            app.database.dao().plants().first().filter { it.id.startsWith(prefix) }.forEach { app.database.dao().delete(it) }
        }
        rule.waitForIdle()
    }
    @After fun cleanup() {
        runBlocking {
            app.settings.set("imperial", false)
            app.database.dao().plants().first().filter { it.id.startsWith(prefix) }.forEach { app.database.dao().delete(it) }
        }
        photos.forEach { it.delete() }
    }
    private fun nav(label: String) {
        val node = rule.onNodeWithText(label, substring=false)
        if (rule.activity.resources.configuration.screenWidthDp >= 720) node.performScrollTo()
        node.performClick()
    }
    private fun waitText(label: String) {
        rule.waitUntil(10000) { rule.onAllNodesWithText(label, substring=false).fetchSemanticsNodes().isNotEmpty() }
    }
    @Test fun categoryNameSearchAndReverseSort() {
        val a = Plant(id=prefix+"a", name="Alpha fern", category="Indoor", location="Office")
        val z = Plant(id=prefix+"z", name="Zebra herb", category="Herb", location="Kitchen")
        runBlocking { app.database.dao().save(a); app.database.dao().save(z) }
        waitText(a.name)
        val search = rule.onNodeWithText("Search plants, species or locations")
        search.performTextInput("herb")
        rule.onNodeWithText(z.name).assertExists()
        rule.onNodeWithText(a.name).assertDoesNotExist()
        search.performTextReplacement("Alpha")
        rule.onNodeWithText(a.name).assertExists()
        rule.onNodeWithText(z.name).assertDoesNotExist()
        search.performTextClearance()
        rule.onNodeWithText("Herbs", substring=false).performScrollTo().performClick()
        rule.onNodeWithText(z.name).assertExists()
        rule.onNodeWithText(a.name).assertDoesNotExist()
        rule.onNodeWithText("All", substring=false).performScrollTo().performClick()
        fun position(name: String) = rule.onNodeWithText(name).fetchSemanticsNode().positionInRoot.let { it.y * 10000 + it.x }
        assertTrue(position(a.name) < position(z.name))
        rule.onNodeWithText("Name: A–Z").performClick()
        rule.waitForIdle()
        assertTrue(position(z.name) < position(a.name))
    }
    @Test fun careTypesSavedPhotosMeasurementsAndRecreation() {
        val plant = Plant(id=prefix+"journal", name="Release journal fern")
        runBlocking { app.database.dao().save(plant) }
        nav("Calendar")
        for ((label, key) in listOf("Watering check" to "water", "Fertilising check" to "fertilise", "Pruning" to "prune", "Repotting" to "repot")) {
            rule.onNodeWithText("Add care schedule").performClick()
            rule.onNode(hasText(label, substring=false) and hasAnyAncestor(isDialog())).performScrollTo().performClick()
            rule.onNodeWithText("Save", substring=false).performClick()
            rule.waitUntil(10000) { runBlocking { app.database.dao().schedules().first().any { it.plantId==plant.id && it.type==key } } }
        }
        val entries = listOf("2026-01-01" to "Earlier saved photo", "2026-02-01" to "Latest saved photo").mapIndexed { i, (date,note) ->
            val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888).apply { eraseColor(if(i==0) android.graphics.Color.GREEN else android.graphics.Color.BLUE) }
            val path = Photos.save(app, bitmap); bitmap.recycle(); photos += File(path)
            JournalEntry(id=prefix+"entry$i", plantId=plant.id, date=date, note=note, heightCm=25.4, photo=path)
        }
        runBlocking { entries.forEach { app.database.dao().save(it) } }
        assertEquals(entries.reversed().map { it.id }, runBlocking { app.database.dao().journal().first().filter { it.plantId==plant.id }.map { it.id } })
        rule.activityRule.scenario.recreate()
        waitText(plant.name)
        nav("Journal")
        rule.onNodeWithText("Latest saved photo").assertExists()
        rule.onAllNodesWithText("Height: 25.4 cm").assertCountEquals(2)
        rule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("First and latest saved photographs · ${plant.name}"))
        rule.onNodeWithText("First and latest saved photographs · ${plant.name}").assertIsDisplayed()
        assertTrue(photos.all { it.exists() && it.length()>0 })
        runBlocking { app.settings.set("imperial", true) }
        rule.waitForIdle()
        for (entry in entries) {
            rule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(entry.note))
            rule.onNode(hasText(entry.note) and hasText("Height: 10 in")).assertExists()
        }
    }
}
