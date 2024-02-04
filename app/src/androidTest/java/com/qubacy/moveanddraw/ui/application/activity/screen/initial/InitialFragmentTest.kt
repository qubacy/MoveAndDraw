package com.qubacy.moveanddraw.ui.application.activity.screen.initial

import android.view.View
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.textview.MaterialTextView
import org.junit.Before
import org.junit.Test
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw._common._test.util.launcher.launchFragmentInHiltContainer
import com.qubacy.moveanddraw.ui._common._test.view.util.action.drag.DragViewAction
import com.qubacy.moveanddraw.ui._common._test.view.util.action.drag.DragViewActionUtil
import com.qubacy.moveanddraw.ui._common._test.view.util.action.swipe.SwipeViewActionUtil
import com.qubacy.moveanddraw.ui._common._test.view.util.action.wait.WaitViewAction
import com.qubacy.moveanddraw.ui.application.activity.screen.common.component.button.draggable.view.DraggableButton
import com.qubacy.moveanddraw.ui.application.activity.screen.initial.component.chooser.view.OptionChooserComponent
import com.qubacy.moveanddraw.ui.application.activity.screen.initial.model.InitialViewModelModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(InitialViewModelModule::class)
class InitialFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var mFragment: InitialFragment
    private lateinit var mNavController: TestNavHostController

    @Before
    fun setup() {
        mNavController = TestNavHostController(ApplicationProvider.getApplicationContext())

        launchFragmentInHiltContainer<InitialFragment> {
            mNavController.setGraph(R.navigation.nav_graph)
            Navigation.setViewNavController(requireView(), mNavController)

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
        Espresso.onView(isRoot()).perform(WaitViewAction(1000))
        Espresso.onView(withId(R.id.fragment_initial_option_chooser))
            .check(ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun scrimWithOptionsDisappearsOnScrimClickedTest() {
        Espresso.onView(withId(R.id.fragment_initial_button_start))
            .perform(ViewActions.click())
        Espresso.onView(withId(R.id.fragment_initial_option_chooser))
            .perform(ViewActions.click(), WaitViewAction(1000))
            .check(ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    private class SwipeWithOptionTitleCheckViewAction(
        private val mEndX: Float,
        private val mEndY: Float,
        private val mTitle: MaterialTextView,
        private val mExpectedTitle: String
    ) : ViewAction {

        override fun getDescription(): String = String()

        override fun getConstraints(): Matcher<View> = Matchers.any(View::class.java)

        override fun perform(uiController: UiController?, view: View?) {
            DragViewActionUtil.drag(uiController!!, view!!, mEndX, mEndY) {
                if (!mTitle.text.equals(mExpectedTitle)) throw IllegalStateException()
            }
        }
    }

    @Test
    fun swipingChoosingButtonRightAndLeftLeadsToTitleChangesTest() {
        Espresso.onView(withId(R.id.fragment_initial_button_start))
            .perform(ViewActions.click())

        val screenWidth = mFragment.requireView().measuredWidth
        val dragButton = mFragment.requireView()
            .findViewById<DraggableButton>(R.id.component_option_chooser_swipe_button)
        val optionTitles = InstrumentationRegistry.getInstrumentation().targetContext.resources
            .getStringArray(R.array.component_option_chooser_options_titles)
        val optionChooserHeader = mFragment.requireView()
            .findViewById<MaterialTextView>(R.id.component_option_chooser_header)

        val dragButtonX = dragButton.x
        val dragButtonY = dragButton.y

        val rightPathEdgeLength = screenWidth - dragButtonX - dragButton.measuredWidth / 2f
        val leftPathEdgeLength = screenWidth - rightPathEdgeLength

        val rightOptionPathPreviewLength = rightPathEdgeLength *
                OptionChooserComponent.DEFAULT_SWIPE_PREVIEW_PERCENT
        val leftOptionPathPreviewLength = leftPathEdgeLength *
                OptionChooserComponent.DEFAULT_SWIPE_PREVIEW_PERCENT

        Espresso.onView(withId(R.id.component_option_chooser_swipe_button))
            .perform(SwipeWithOptionTitleCheckViewAction(
                dragButtonX - leftOptionPathPreviewLength,
                dragButtonY, optionChooserHeader, optionTitles[0])
            )
        Espresso.onView(withId(R.id.component_option_chooser_header))
            .check(ViewAssertions.matches(
                ViewMatchers.withText(R.string.component_option_chooser_header_text)))
        Espresso.onView(withId(R.id.component_option_chooser_swipe_button))
            .perform(SwipeWithOptionTitleCheckViewAction(
                dragButtonX + rightOptionPathPreviewLength,
                dragButtonY, optionChooserHeader, optionTitles[1])
            )
    }

    @Test
    fun choosingOptionLeadsToScrimDisappearanceTest() {
        Espresso.onView(withId(R.id.fragment_initial_button_start))
            .perform(ViewActions.click())

        val screenWidth = mFragment.requireView().measuredWidth
        val dragButton = mFragment.requireView()
            .findViewById<DraggableButton>(R.id.component_option_chooser_swipe_button)

        val dragButtonX = dragButton.x
        val dragButtonY = dragButton.y

        val rightPathEdgeLength = screenWidth - dragButtonX
        val leftPathEdgeLength = screenWidth - rightPathEdgeLength  + dragButton.measuredWidth / 2f

        val rightOptionPathActivationLength = rightPathEdgeLength *
                OptionChooserComponent.DEFAULT_SWIPE_ACTIVATION_PERCENT
        val leftOptionPathActivationLength = leftPathEdgeLength *
                OptionChooserComponent.DEFAULT_SWIPE_ACTIVATION_PERCENT

        Espresso.onView(withId(R.id.component_option_chooser_swipe_button))
            .perform(
                DragViewAction(dragButtonX - leftOptionPathActivationLength, dragButtonY),
                WaitViewAction(1000)
            )
        Espresso.onView(withId(R.id.fragment_initial_option_chooser))
            .check(ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))

        runBlocking(Dispatchers.Main) {
            mNavController.setCurrentDestination(R.id.initialFragment)
        }

        Espresso.onView(withId(R.id.fragment_initial_button_start))
            .perform(ViewActions.click())

        Espresso.onView(withId(R.id.component_option_chooser_swipe_button))
            .perform(
                DragViewAction(dragButtonX + rightOptionPathActivationLength, dragButtonY),
                WaitViewAction(1000)
            )
        Espresso.onView(withId(R.id.fragment_initial_option_chooser))
            .check(ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

//    @Test
//    fun choosingDrawingLeadsToTransitionToCalibrationScreenTest() {
//        Espresso.onView(withId(R.id.fragment_initial_button_start))
//            .perform(ViewActions.click())
//        Espresso.onView(withId(R.id.component_option_chooser_swipe_button))
//            .perform(SwipeViewActionUtil.generateSwipeViewAction(0f, 0f))
//
//        Assert.assertEquals(R.id.calibrationFragment, mNavController.currentDestination!!.id)
//    }

    @Test
    fun choosingViewingLeadsToTransitionToViewerScreenTest() {
        Espresso.onView(withId(R.id.fragment_initial_button_start))
            .perform(ViewActions.click())

        val endX = mFragment.requireView().measuredWidth.toFloat()

        Espresso.onView(withId(R.id.component_option_chooser_swipe_button))
            .perform(SwipeViewActionUtil.generateSwipeViewAction(endX, 0f))

        Assert.assertEquals(R.id.viewerFragment, mNavController.currentDestination!!.id)
    }
}