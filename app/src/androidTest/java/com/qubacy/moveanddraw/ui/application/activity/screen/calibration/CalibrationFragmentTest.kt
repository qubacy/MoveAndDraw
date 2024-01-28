package com.qubacy.moveanddraw.ui.application.activity.screen.calibration

import androidx.lifecycle.ViewModelLazy
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw._common._test.util.launcher.launchFragmentInHiltContainer
import com.qubacy.moveanddraw.ui._common._test.view.util.action.click.SimpleClickViewAction
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment._common.StatefulFragmentTest
import com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model.CalibrationViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model.CalibrationViewModelFactoryModule
import com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model.state.CalibrationUiState
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.Field

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(CalibrationViewModelFactoryModule::class)
class CalibrationFragmentTest(

) : StatefulFragmentTest<CalibrationUiState, CalibrationViewModel, CalibrationFragment>() {
    override fun retrieveModelFieldReflection(): Field {
        return CalibrationViewModel::class.java
            .getDeclaredField("mModel\$delegate")
            .apply { isAccessible = true }
    }

    override fun initFragment(modelFieldReflection: Field) {
        launchFragmentInHiltContainer<CalibrationFragment> {
            mNavController.setGraph(R.navigation.nav_graph)
            Navigation.setViewNavController(requireView(), mNavController)

            mFragment = this as CalibrationFragment
            mModel = (modelFieldReflection.get(mFragment) as ViewModelLazy<CalibrationViewModel>).value
        }
    }

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun allComponentsAreDisplayedTest() {
        Espresso.onView(ViewMatchers.withId(R.id.fragment_calibration_header))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.fragment_calibration_description))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.fragment_calibration_phone_image))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.fragment_calibration_button_start))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun executeCalibrationTest() {
        Espresso.onView(ViewMatchers.withId(R.id.fragment_calibration_header))
            .check(ViewAssertions.matches(
                ViewMatchers.withText(R.string.fragment_calibration_header_text_idle)))
        Espresso.onView(ViewMatchers.withId(R.id.fragment_calibration_description))
            .check(ViewAssertions.matches(
                ViewMatchers.withText(R.string.fragment_calibration_description_text_idle)))
        Espresso.onView(ViewMatchers.withId(R.id.fragment_calibration_button_start))
            .check(ViewAssertions.matches(
                Matchers.allOf(
                    ViewMatchers.withText(R.string.fragment_calibration_button_start_caption_idle),
                    ViewMatchers.isEnabled()
                )))

        Espresso.onView(ViewMatchers.withId(R.id.fragment_calibration_button_start))
            .perform(SimpleClickViewAction())

        setState(CalibrationUiState(CalibrationUiState.State.CALIBRATING))

        Espresso.onView(ViewMatchers.withId(R.id.fragment_calibration_description))
            .check(ViewAssertions.matches(
                ViewMatchers.withText(R.string.fragment_calibration_description_text_calibrating)))
        Espresso.onView(ViewMatchers.withId(R.id.fragment_calibration_button_start))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isEnabled())))

        setState(CalibrationUiState(CalibrationUiState.State.CALIBRATED))

        Espresso.onView(ViewMatchers.withId(R.id.fragment_calibration_header))
            .check(ViewAssertions.matches(
                ViewMatchers.withText(R.string.fragment_calibration_header_text_calibrated)))
        Espresso.onView(ViewMatchers.withId(R.id.fragment_calibration_description))
            .check(ViewAssertions.matches(
                ViewMatchers.withText(R.string.fragment_calibration_description_text_calibrated)))
        Espresso.onView(ViewMatchers.withId(R.id.fragment_calibration_button_start))
            .check(ViewAssertions.matches(
                Matchers.allOf(
                    ViewMatchers.withText(R.string.fragment_calibration_button_start_caption_calibrated),
                    ViewMatchers.isEnabled()
                )
            ))
    }

    @Test
    fun clickingLetsGoButtonLeadsToTransitionToEditorScreenTest() {
        setState(CalibrationUiState(CalibrationUiState.State.CALIBRATED))

        Espresso.onView(ViewMatchers.withId(R.id.fragment_calibration_button_start))
            .perform(SimpleClickViewAction())

        // todo: implement after creating a new screen!!


    }
}