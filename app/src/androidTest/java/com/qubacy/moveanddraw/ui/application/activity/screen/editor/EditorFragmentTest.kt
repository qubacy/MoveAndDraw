package com.qubacy.moveanddraw.ui.application.activity.screen.editor

import android.util.TypedValue
import androidx.lifecycle.ViewModelLazy
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw._common._test.util.launcher.launchFragmentInHiltContainer
import com.qubacy.moveanddraw.ui._common._test.view.util.action.wait.WaitViewAction
import com.qubacy.moveanddraw.ui._common._test.view.util.matcher._common.icon.tint.IconTintViewMatcher
import com.qubacy.moveanddraw.ui._common._test.view.util.matcher.button.floating.icon.IconDrawableViewMatcher
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment._common.StatefulFragmentTest
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.EditorViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.EditorViewModelFactoryModule
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.EditorUiState
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.Field


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@UninstallModules(EditorViewModelFactoryModule::class)
class EditorFragmentTest(

) : StatefulFragmentTest<EditorUiState, EditorViewModel, EditorFragment>() {


    override fun retrieveModelFieldReflection(): Field {
        return EditorFragment::class.java
            .getDeclaredField("mModel\$delegate")
            .apply { isAccessible = true }
    }

    override fun initFragment(modelFieldReflection: Field) {
        launchFragmentInHiltContainer<EditorFragment> {
            mNavController.setGraph(R.navigation.nav_graph)
            Navigation.setViewNavController(requireView(), mNavController)

            mFragment = this as EditorFragment
            mModel = (modelFieldReflection.get(mFragment) as ViewModelLazy<EditorViewModel>).value
        }
    }

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun allComponentsAreDisplayedTest() {
        Espresso.onView(withId(R.id.fragment_editor_top_bar))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.fragment_editor_bottom_bar))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.fragment_editor_button_main_action))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.fragment_editor_canvas))
            .check(ViewAssertions.matches(isDisplayed()))

        // checking top menu in details:

        Espresso.onView(withId(R.id.drawing_top_bar_share))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.drawing_top_bar_load))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.editor_top_bar_save))
            .check(ViewAssertions.matches(isDisplayed()))

        // checking bottom menu in details:

        Espresso.onView(withId(R.id.editor_bottom_bar_pick_color))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.editor_bottom_bar_drawing_mode))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.editor_bottom_bar_undo_face))
            .check(ViewAssertions.matches(isDisplayed()))
    }

    // todo: preserve for End-to-End testing:
//    @Test
//    fun clickingShareWithoutSettingDrawingLeadsToShowingErrorTest() {
//        Espresso.onView(withId(R.id.drawing_top_bar_share))
//            .perform(ViewActions.click(), WaitViewAction(1000))
//        Espresso.onView(withText(R.string.component_error_dialog_title))
//            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//    }

    // todo: preserve for End-to-End testing:
//    @Test
//    fun clickingSaveWithoutSettingDrawingLeadsToShowingErrorTest() {
//
//    }

    // Note: there's no need to do it using a color picking dialog;
    @Test
    fun pickingDrawingColorLeadsToPickingColorIconTintChangeTest() {
        val onColorPickedMethodReflection = EditorFragment::class.java
            .getDeclaredMethod("onColorPicked", Int::class.java)
            .apply { isAccessible = true }

        val pickedDrawingColor = InstrumentationRegistry.getInstrumentation()
            .targetContext.getColor(android.R.color.holo_purple)

        onColorPickedMethodReflection.invoke(mFragment, pickedDrawingColor)

        Espresso.onView(withId(R.id.editor_bottom_bar_pick_color))
            .check(ViewAssertions.matches(IconTintViewMatcher(pickedDrawingColor)))
    }

    @Test
    fun clickingCreateFaceLeadsToEditorModeChangeTest() {
        Espresso.onView(withId(R.id.fragment_editor_button_main_action))
            .perform(ViewActions.click(), WaitViewAction(1000))

        Espresso.onView(withId(R.id.editor_bottom_bar_undo_vertex))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.editor_bottom_bar_cancel))
            .check(ViewAssertions.matches(isDisplayed()))

        Espresso.onView(withId(R.id.editor_bottom_bar_pick_color))
            .check(ViewAssertions.doesNotExist())
        Espresso.onView(withId(R.id.editor_bottom_bar_undo_face))
            .check(ViewAssertions.doesNotExist())

        val typedValue = TypedValue()

        Assert.assertTrue(
            mFragment.requireContext().theme.resolveAttribute(
                com.google.android.material.R.attr.colorOnPrimaryContainer, typedValue, true)
        )

        val iconTint = typedValue.data

        Espresso.onView(withId(R.id.fragment_editor_button_main_action))
            .check(ViewAssertions.matches(IconDrawableViewMatcher(
                R.drawable.ic_check, iconTint)))
    }


}