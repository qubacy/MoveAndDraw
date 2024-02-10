package com.qubacy.moveanddraw.ui.application.activity.screen.viewer

import androidx.lifecycle.ViewModelLazy
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoActivityResumedException
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw._common._test.util.launcher.launchFragmentInHiltContainer
import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.domain._common.model.drawing._test.util.DrawingGeneratorUtil
import com.qubacy.moveanddraw.ui._common._test.view.util.action.wait.WaitViewAction
import com.qubacy.moveanddraw.ui._common._test.view.util.matcher.button.navigation.NavigationButtonViewMatcher
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing.DrawingFragmentTest
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.view.CanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.operation.loaded.DrawingLoadedUiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.ViewerViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.ViewerViewModelFactoryModule
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.state.ViewerUiState
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Exception
import java.lang.reflect.Field

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@UninstallModules(ViewerViewModelFactoryModule::class)
class ViewerFragmentTest(

) : DrawingFragmentTest<ViewerUiState, ViewerViewModel, CanvasView, ViewerFragment>() {
    override fun retrieveModelFieldReflection(): Field {
        return ViewerFragment::class.java
            .getDeclaredField("mModel\$delegate")
            .apply { isAccessible = true }
    }

    override fun initFragment(modelFieldReflection: Field) {
        launchFragmentInHiltContainer<ViewerFragment> {
            mNavController.setGraph(R.navigation.nav_graph)
            Navigation.setViewNavController(requireView(), mNavController)

            mFragment = this as ViewerFragment
            mModel = (modelFieldReflection.get(mFragment) as ViewModelLazy<ViewerViewModel>).value
        }
    }

    @Before
    override fun setup() {
        super.setup()
    }

    override fun generateUiStateWithUiOperation(operation: UiOperation): ViewerUiState {
        return ViewerUiState(pendingOperations = TakeQueue(operation))
    }

    @Test
    override fun allComponentsAreDisplayedTest() {
        super.allComponentsAreDisplayedTest()

        Espresso.onView(withId(R.id.fragment_viewer_top_bar))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.fragment_viewer_canvas))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.fragment_viewer_entry_icon))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.fragment_viewer_entry_message))
            .check(ViewAssertions.matches(isDisplayed()))
    }

    @Test
    fun leaveFragmentOnUpNavigationButtonPressed() {
        Espresso.onView(Matchers.allOf(
            NavigationButtonViewMatcher(R.drawable.ic_go_back)
        )).perform(WaitViewAction(500), ViewActions.click())

        try {
            Espresso.onView(isRoot()).check(ViewAssertions.matches(isDisplayed()))

        } catch (e: Exception) {
            if (e::class == NoActivityResumedException::class) return

            throw e
        }
    }

    @Test
    fun loadingDrawingStateChangingTest() {
        Espresso.onView(withId(R.id.fragment_viewer_entry_icon))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.fragment_viewer_entry_message))
            .check(ViewAssertions.matches(isDisplayed()))

        val loadingState = ViewerUiState(isLoading = true)

        setState(loadingState)

        Espresso.onView(withId(R.id.fragment_viewer_progress_indicator))
            .check(ViewAssertions.matches(isDisplayed()))

        val drawing = DrawingGeneratorUtil.generateSquareDrawing()
        val state = generateUiStateWithUiOperation(DrawingLoadedUiOperation(drawing))

        setState(state)

        Espresso.onView(withId(R.id.fragment_viewer_progress_indicator))
            .check(ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        Espresso.onView(withId(R.id.fragment_viewer_entry_icon))
            .check(ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        Espresso.onView(withId(R.id.fragment_viewer_entry_message))
            .check(ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }
}