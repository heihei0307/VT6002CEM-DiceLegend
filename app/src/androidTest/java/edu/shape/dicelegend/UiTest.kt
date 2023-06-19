package edu.shape.dicelegend


import androidx.test.espresso.DataInteraction
import androidx.test.espresso.ViewInteraction
import androidx.test.filters.LargeTest
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent

import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*

import edu.shape.dicelegend.R

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anything
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.startsWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class UiTest {

    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun uiTest() {
        val textView = onView(
allOf(withId(R.id.txtUserCode), withText(startsWith("User Code:")),
withParent(allOf(withId(R.id.container),
withParent(withId(android.R.id.content)))),
isDisplayed()))
        textView.check(matches(isDisplayed()))
        
        val textView2 = onView(
allOf(withId(R.id.weather), withText(startsWith("Weather:")),
withParent(allOf(withId(R.id.container),
withParent(withId(android.R.id.content)))),
isDisplayed()))
        textView2.check(matches(isDisplayed()))
        }
    }
