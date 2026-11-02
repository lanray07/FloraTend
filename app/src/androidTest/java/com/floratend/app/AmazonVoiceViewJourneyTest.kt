package com.floratend.app

import android.app.UiAutomation
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.security.MessageDigest

/** External accessibility actions on the delivered app. Only this run's unique record is removed. */
class AmazonVoiceViewJourneyTest {
    @Test fun deliveredAmazon15EditableJourneyWithRealVoiceView() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        check(context.packageName == "com.floratend.app.qa")
        check(InstrumentationRegistry.getArguments().getString("floratend.amazonVoiceView") == "true")
        val target = "com.floratend.app"
        val info = context.packageManager.getPackageInfo(target, android.content.pm.PackageManager.GET_SIGNATURES)
        assertEquals(15, info.versionCode)
        assertEquals(0, info.applicationInfo!!.flags and ApplicationInfo.FLAG_DEBUGGABLE)
        assertEquals("com.amazon.venezia", context.packageManager.getInstallerPackageName(target))
        assertEquals("8e35b165d5a1dc14c10820cee25ed2c24e9c543ceb2f059b9f5bc93baa672297",
            MessageDigest.getInstance("SHA-256").digest(info.signatures!![0].toByteArray()).joinToString("") { "%02x".format(it) })
        assertTrue(Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty().contains("com.amazon.logan"))
        val automation = instrumentation.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
        val cleanupFixture = InstrumentationRegistry.getArguments().getString("floratend.cleanupVoiceViewFixture")
        check(cleanupFixture == null || cleanupFixture.matches(Regex("VoiceView QA [a-f0-9]{8}( edited)?")))
        val fixture = cleanupFixture ?: ("VoiceView QA " + java.util.UUID.randomUUID().toString().take(8))
        val results = JSONArray()
        val folder = File(context.getExternalFilesDir(null), "accessibility/amazon-journey").apply { mkdirs() }
        val report = JSONObject().put("fixtureName", fixture).put("amazonVersionCode", 15)
            .put("amazonTargetInstrumented", false).put("realVoiceViewEnabled", true)
            .put("actions", results).put("fixtureRemoved", false).put("result", "IN_PROGRESS")
        fun checkpoint() { File(folder, "results.json").writeText(report.toString(2)) }
        fun nodes(): List<AccessibilityNodeInfo> {
            val root = automation.rootInActiveWindow ?: return emptyList()
            if (root.packageName?.toString() != target) return emptyList()
            val result = mutableListOf<AccessibilityNodeInfo>()
            fun walk(node: AccessibilityNodeInfo) { result += node; repeat(node.childCount) { node.getChild(it)?.let(::walk) } }
            walk(root)
            return result
        }
        fun find(label: String, scroll: Boolean = false, action: Boolean = false): AccessibilityNodeInfo {
            val until = System.currentTimeMillis() + 12000
            var lastScroll = 0L
            do {
                val current = nodes()
                current.firstOrNull { (!action || !it.isEditable) && (it.text?.toString()?.trim() == label || it.contentDescription?.toString() == label || it.contentDescription?.toString()?.startsWith(label + ".") == true) }?.let { return it }
                if (scroll && System.currentTimeMillis() - lastScroll > 600) {
                    current.filter { it.isScrollable }.forEach { it.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) }
                    lastScroll = System.currentTimeMillis()
                }
                Thread.sleep(80)
            } while (System.currentTimeMillis() < until)
            error("Accessibility control missing: $label")
        }
        fun focus(node: AccessibilityNodeInfo, label: String) {
            assertTrue("$label enabled", node.isEnabled)
            assertTrue("$label focus", node.isAccessibilityFocused || node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS))
        }
        fun activate(label: String, scroll: Boolean = false) {
            var node = find(label, scroll, action = true)
            // The label and its clickable parent can be separate native accessibility nodes.
            // Focus a currently exposed labelled node; click its semantic action ancestor.
            var focused = false
            repeat(20) {
                if (!focused) {
                    node = find(label, scroll, action = true)
                    var candidate: AccessibilityNodeInfo? = node
                    while (candidate != null && !focused) {
                        focused = candidate.isAccessibilityFocused || candidate.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)
                        if (candidate.isClickable) break
                        candidate = candidate.parent
                    }
                    if (!focused) Thread.sleep(100)
                }
            }
            assertTrue("$label accessibility focus", focused)
            // VoiceView focus events can refresh Compose's virtual-node generation.
            node = find(label, scroll, action = true)
            while (!node.isClickable && node.parent != null) node = node.parent
            assertTrue("$label enabled", node.isEnabled)
            assertTrue("$label click", node.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            results.put(JSONObject().put("label", label).put("accessibilityFocus", true).put("click", true))
            checkpoint()
            instrumentation.waitForIdleSync()
        }
        fun replaceFirstEditor(value: String) {
            val until = System.currentTimeMillis() + 10000
            var node = nodes().firstOrNull { it.isEditable }
            while (node == null && System.currentTimeMillis() < until) {
                Thread.sleep(80)
                node = nodes().firstOrNull { it.isEditable }
            }
            checkNotNull(node) { "Editable field did not become available" }
            focus(node, "Editable text")
            assertTrue(node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value)
            }))
            instrumentation.waitForIdleSync()
        }
        checkpoint()
        context.startActivity(context.packageManager.getLaunchIntentForPackage(target)!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        Thread.sleep(500)
        try {
            find("A little care. A greener home.")
            if (cleanupFixture != null) {
                replaceFirstEditor(fixture)
                activate(fixture)
                find(fixture)
                activate("Delete", true)
                find("Delete this plant?")
                activate("Delete")
                find("A little care. A greener home.")
                Thread.sleep(300)
                assertFalse(nodes().any { it.text?.toString() == fixture && !it.isEditable })
                replaceFirstEditor("")
                report.put("fixtureRemoved", true).put("result", "PASS_CONTROLLED_RECOVERY")
                checkpoint()
                return
            }
            activate("Add plant")
            find("Plant details")
            replaceFirstEditor(fixture)
            activate("Save")
            find("A little care. A greener home.")
            replaceFirstEditor(fixture) // Garden search limits every subsequent selection to our unique record.
            activate(fixture)
            activate("Edit")
            find("Plant details")
            replaceFirstEditor(fixture + " edited")
            activate("Save")
            find("A little care. A greener home.")
            activate(fixture + " edited")
            activate("Close")
            activate("Calendar")
            find("Your care calendar")
            activate("Add care schedule")
            activate(fixture + " edited", true) // Explicitly choose only our run's plant.
            find("Interval in days (1–3650)", true)
            activate("Save")
            find("Your care calendar")
            activate(fixture + " edited") // Filter care actions to our plant.
            activate("Record care")
            find("Record care")
            activate("Soil checked: dry")
            activate("Next 7 days")
            activate("Pause")
            activate("Resume")
            activate("Snooze until tomorrow")
            activate("Reschedule")
            find("Date (YYYY-MM-DD)")
            activate("Cancel")
            activate("Journal")
            find("Every new leaf has a story")
            activate("Add journal entry")
            find("Notes and observations", true)
            activate("Cancel")
            activate("Voice")
            find("A helping voice")
            replaceFirstEditor("Unrecognised accessibility test command")
            activate("Run command")
            val error = find("Command not recognised. Try one of the examples below, or use the app controls.")
            assertTrue(error.isAccessibilityFocused || error.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS))
            replaceFirstEditor("")
            activate("Insights")
            find("Your records at a glance. Activity does not prove plant health.")
            activate("Settings")
            find("Make FloraTend feel at home.")
            activate("Language: Device language")
            find("Language")
            activate("Close")
            activate("Garden")
            replaceFirstEditor(fixture)
            activate(fixture + " edited")
            // The selected title is the exact unique record we created; no bulk-delete control is used.
            find(fixture + " edited")
            activate("Delete", true)
            find("Delete this plant?")
            activate("Delete")
            find("A little care. A greener home.")
            Thread.sleep(300)
            assertFalse(nodes().any { it.text?.toString()?.contains(fixture) == true && !it.isEditable })
            replaceFirstEditor("")
            report.put("fixtureRemoved", true).put("createdAndEditedRecordReopened", true)
                .put("unknownCommandErrorAccessible", true).put("result", "PASS")
                .put("ownPlantCareControlsExercised", true)
                .put("scope", "Real VoiceView with native accessibility focus/click/set-text on delivered Amazon15. Own unique plant created, edited, reopened and removed with its care schedule/history. Care recorded, paused/resumed, snoozed; reschedule and journal editors cancelled. No audio/pronunciation or manual gesture certification.")
            checkpoint()
        } catch (failure: Throwable) {
            report.put("result", "FAIL").put("failure", failure.javaClass.simpleName)
            checkpoint()
            throw failure
        }
    }
}
