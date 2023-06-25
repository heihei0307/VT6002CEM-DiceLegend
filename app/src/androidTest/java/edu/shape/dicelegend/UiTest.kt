package edu.shape.dicelegend


import androidx.test.filters.LargeTest
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import org.hamcrest.Matchers.allOf
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
