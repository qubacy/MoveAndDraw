package com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.ui._common._test.view.util.action.wait.WaitViewAction
import com.qubacy.moveanddraw.ui._common._test.view.util.matcher.menu.icon.drawable.MenuItemIconDrawableViewMatcher
import com.qubacy.moveanddraw.ui._common._test.view.util.matcher.menu.icon.tint.MenuItemIconTintViewMatcher
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment._common.StatefulFragmentTest
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.DrawingFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.view.CanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.DrawingViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.DrawingUiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.operation._common.SetDrawingUiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.EditorFragment
import org.junit.Test
import java.lang.reflect.Field

abstract class DrawingFragmentTest<
        UiStateType : DrawingUiState,
        ViewModelType : DrawingViewModel<UiStateType>,
        CanvasViewType : CanvasView,
        FragmentType : DrawingFragment<UiStateType, ViewModelType, CanvasViewType>
>() : StatefulFragmentTest<UiStateType, ViewModelType, FragmentType>() {
    private lateinit var mDrawingFieldReflection: Field

    protected open fun setModelDrawingBySetDrawingStateOperation(drawing: Drawing) {
        setModelDrawing(drawing)

        val state = generateUiStateWithUiOperation(SetDrawingUiOperation(drawing))

        setState(state)
    }

    protected open fun setModelDrawing(drawing: Drawing) {
        mDrawingFieldReflection.set(mModel, drawing)
    }

    override fun mockViewModel() {
        super.mockViewModel()

        mDrawingFieldReflection = DrawingViewModel::class.java.getDeclaredField("mDrawing")
            .apply { isAccessible = true }
    }

    open fun allComponentsAreDisplayedTest() {
        // checking top menu in details:

        Espresso.onView(ViewMatchers.withId(R.id.drawing_top_bar_share))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.drawing_top_bar_load))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // checking bottom menu in details:

        Espresso.onView(ViewMatchers.withId(R.id.drawing_bottom_bar_pick_color))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.drawing_bottom_bar_drawing_mode))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    // Note: there's no need to do it using a color picking dialog;
    @Test
    fun pickingDrawingColorLeadsToPickingColorIconTintChangeTest() {
        val onColorPickedMethodReflection = DrawingFragment::class.java
            .getDeclaredMethod("onColorPicked", Int::class.java)
            .apply { isAccessible = true }

        val pickedDrawingColor = InstrumentationRegistry.getInstrumentation()
            .targetContext.getColor(android.R.color.holo_purple)

        onColorPickedMethodReflection.invoke(mFragment, pickedDrawingColor)

        Espresso.onView(ViewMatchers.withId(R.id.drawing_bottom_bar_pick_color))
            .check(ViewAssertions.matches(MenuItemIconTintViewMatcher(pickedDrawingColor)))
    }

    @Test
    fun clickingDrawingModeLeadsToDrawingModeButtonIconChangeTest() {
        Espresso.onView(ViewMatchers.withId(R.id.drawing_bottom_bar_drawing_mode))
            .check(ViewAssertions.matches(
                MenuItemIconDrawableViewMatcher(R.drawable.ic_square)
            ))

        Espresso.onView(ViewMatchers.withId(R.id.drawing_bottom_bar_drawing_mode))
            .perform(WaitViewAction(200), ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.drawing_bottom_bar_drawing_mode))
            .check(ViewAssertions.matches(
                MenuItemIconDrawableViewMatcher(R.drawable.ic_outlined_square)
            ))

        Espresso.onView(ViewMatchers.withId(R.id.drawing_bottom_bar_drawing_mode))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.drawing_bottom_bar_drawing_mode))
            .check(ViewAssertions.matches(
                MenuItemIconDrawableViewMatcher(R.drawable.ic_mesh)
            ))

        Espresso.onView(ViewMatchers.withId(R.id.drawing_bottom_bar_drawing_mode))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.drawing_bottom_bar_drawing_mode))
            .check(ViewAssertions.matches(
                MenuItemIconDrawableViewMatcher(R.drawable.ic_square)
            ))
    }
}