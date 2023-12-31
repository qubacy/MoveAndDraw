package com.qubacy.moveanddraw.ui.application.activity.screen.calibration

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelLazy
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw._common.util.launcher.launchFragmentInHiltContainer
import com.qubacy.moveanddraw.ui._common.util.view.action.click.SimpleClickViewAction
import com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model.CalibrationViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model.CalibrationViewModelFactoryModule
import com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model.state.CalibrationUiState
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(CalibrationViewModelFactoryModule::class)//CalibrationViewModelModule::class)
class CalibrationFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var mFragment: CalibrationFragment

    private lateinit var mModel: CalibrationViewModel
    private lateinit var mUiState: MutableLiveData<CalibrationUiState>

    private lateinit var mNavController: TestNavHostController

    private fun setState(uiState: CalibrationUiState) {
        mUiState.postValue(uiState)
    }

    private fun mockViewModel() {
        mUiState = mModel.uiState as MutableLiveData<CalibrationUiState>
    }

    @Before
    fun setup() {
        mNavController = TestNavHostController(ApplicationProvider.getApplicationContext())

        val mModelFieldReflection = CalibrationFragment::class.java
            .getDeclaredField("mModel\$delegate")
            .apply { isAccessible = true }

        launchFragmentInHiltContainer<CalibrationFragment> {
            mNavController.setGraph(R.navigation.nav_graph)
            Navigation.setViewNavController(requireView(), mNavController)

            mFragment = this as CalibrationFragment
            mModel = (mModelFieldReflection.get(mFragment) as ViewModelLazy<CalibrationViewModel>).value
        }

        mockViewModel()
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