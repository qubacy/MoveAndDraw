package com.qubacy.moveanddraw.ui.application.activity.screen.initial

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw._common.util.launcher.launchFragmentInHiltContainer
import com.qubacy.moveanddraw.ui.application.activity.screen.initial.model.InitialViewModelModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(InitialViewModelModule::class)
class InitialFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var mFragment: InitialFragment

    @Before
    fun setup() {
        launchFragmentInHiltContainer<InitialFragment> {
            mFragment = this as InitialFragment
        }
    }

    @Test
    fun allComponentsAreDisplayedTest() {
        Espresso.onView(withId(R.id.fragment_initial_header_title))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.fragment_initial_header_description))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.fragment_initial_drawing_carousel))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.fragment_initial_button_start))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun scrimWithOptionsAppearsOnStartClickedTest() {
        Espresso.onView(withId(R.id.fragment_initial_button_start))
            .perform(ViewActions.click())
        Espresso.onView(withId(R.id.fragment_initial_option_chooser))
            .check(ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @Test
    fun scrimWithOptionsDisappearsOnBackTest() {
        Espresso.onView(withId(R.id.fragment_initial_button_start))
            .perform(ViewActions.click())
        Espresso.pressBack()
        // todo: synchronize this (doesn't work for now):
        Espresso.onView(withId(R.id.fragment_initial_option_chooser))
            .check(ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun scrimWithOptionsDisappearsOnScrimClickedTest() {
        Espresso.onView(withId(R.id.fragment_initial_button_start))
            .perform(ViewActions.click())
        // todo: synchronize this (doesn't work for now):
        Espresso.onView(withId(R.id.fragment_initial_option_chooser))
            .perform(ViewActions.click())
            .check(ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun swipingChoosingButtonRightAndLeftLeadsToTitleChangesTest() {
        Espresso.onView(withId(R.id.fragment_initial_button_start))
            .perform(ViewActions.click())

        // todo: implement a custom view dragging action (or just try):

        Espresso.onView(withId(R.id.component_option_chooser_swipe_button))
            .perform(ViewActions.slowSwipeLeft())


    }
}