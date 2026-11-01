package com.floratend.app

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText

/** Exercise the real platform editor through Espresso in the mixed Compose/View screen. */
fun enterCommand(text:String) { onView(withId(R.id.voice_command)).perform(replaceText(text),closeSoftKeyboard()) }
fun assertCommand(text:String) { onView(withId(R.id.voice_command)).check(matches(withText(text))) }
