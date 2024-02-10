package com.qubacy.moveanddraw.ui.application.activity.screen.editor

import android.net.Uri
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.lifecycle.ViewModelLazy
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw._common._test.util.launcher.launchFragmentInHiltContainer
import com.qubacy.moveanddraw._common._test.util.mock.AnyMockUtil.anyObject
import com.qubacy.moveanddraw._common.error.Error
import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.domain._common.model.drawing._test.util.DrawingGeneratorUtil
import com.qubacy.moveanddraw.ui._common._test.view.util.action.wait.WaitViewAction
import com.qubacy.moveanddraw.ui._common._test.view.util.matcher.button.floating.icon.drawable.FABIconDrawableViewMatcher
import com.qubacy.moveanddraw.ui._common._test.view.util.matcher.toast.root.ToastRootMatcher
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing.DrawingFragmentTest
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation.error.ShowErrorUiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.view.EditorCanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.EditorViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.EditorViewModelFactoryModule
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.EditorUiState
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.operation.saved.DrawingSavedUiOperation
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.lang.reflect.Field

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@UninstallModules(EditorViewModelFactoryModule::class)
class EditorFragmentTest(

) : DrawingFragmentTest<EditorUiState, EditorViewModel, EditorCanvasView, EditorFragment>() {

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

    override fun generateUiStateWithUiOperation(operation: UiOperation): EditorUiState {
        return EditorUiState(pendingOperations = TakeQueue(operation))
    }

    @Test
    override fun allComponentsAreDisplayedTest() {
        super.allComponentsAreDisplayedTest()

        Espresso.onView(withId(R.id.fragment_editor_top_bar))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.fragment_editor_bottom_bar))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.fragment_editor_button_main_action))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.fragment_editor_canvas))
            .check(ViewAssertions.matches(isDisplayed()))

        // checking top menu in details:

        Espresso.onView(withId(R.id.editor_top_bar_save))
            .check(ViewAssertions.matches(isDisplayed()))

        // checking bottom menu in details:

        Espresso.onView(withId(R.id.editor_bottom_bar_undo_face))
            .check(ViewAssertions.matches(isDisplayed()))
    }

    // todo: reimplement in End-to-End testing:
    @Test
    fun clickingShareWithoutSettingDrawingLeadsToShowingErrorTest() {
        mockRetrieveError()

        Espresso.onView(withId(R.id.drawing_top_bar_share))
            .perform(WaitViewAction(1000), ViewActions.click())
        Espresso.onView(withText(TEST_ERROR.message))
            .check(ViewAssertions.matches(isDisplayed()))
    }

    @Test
    fun clickingShareWithNewDrawingSetLeadsToSavingNewFileTest() {
        val filename = "something"
        val filepath = String()

        mockSaveNewDrawing(filepath = filepath)

        val drawing = DrawingGeneratorUtil.generateCubeDrawing()

        setModelDrawingBySetDrawingStateOperation(drawing)

        Espresso.onView(withId(R.id.drawing_top_bar_share))
            .perform(WaitViewAction(1000), ViewActions.click())
        Espresso.onView(withText(R.string.component_dialog_filename_input_header_text))
            .check(ViewAssertions.matches(isDisplayed()))

        Espresso.onView(withId(R.id.component_dialog_filename_text_field))
            .perform(ViewActions.typeText(filename))
        Espresso.onView(withText(R.string.component_dialog_filename_input_button_positive_caption))
            .perform(ViewActions.click())

        Espresso.onView(withText(R.string.fragment_editor_message_saved_image))
            .inRoot(withDecorView(ToastRootMatcher(mFragment.requireActivity())))
            .check(ViewAssertions.matches(isDisplayed()))
    }

    @Test
    fun clickingShareWithExistingDrawingSetLeadsToSavingChangesFileTest() {
        val filepath = String()
        val uri = Uri.parse("")

        mockSaveCurrentDrawingChanges(filepath = filepath)

        val drawing = DrawingGeneratorUtil.generateCubeDrawing(uri)

        setModelDrawingBySetDrawingStateOperation(drawing)

        Espresso.onView(withId(R.id.drawing_top_bar_share))
            .perform(WaitViewAction(1000), ViewActions.click())
        Espresso.onView(withText(R.string.fragment_editor_message_saved_image))
            .inRoot(withDecorView(ToastRootMatcher(mFragment.requireActivity())))
            .check(ViewAssertions.matches(isDisplayed()))
    }

    // todo: reimplement in End-to-End testing:
    @Test
    fun clickingSaveWithoutSettingDrawingLeadsToShowingErrorTest() {
        mockRetrieveError()

        Espresso.onView(withId(R.id.editor_top_bar_save))
            .perform(WaitViewAction(1000), ViewActions.click())
        Espresso.onView(withText(TEST_ERROR.message))
            .check(ViewAssertions.matches(isDisplayed()))
    }

    private fun mockSaveCurrentDrawingChanges(
        filepath: String? = null,
        error: Error? = null
    ) {
        Mockito.`when`(mModel.saveCurrentDrawingChanges(anyObject()))
            .thenAnswer {
                val drawing = it.arguments[0] as Drawing
                val newOperation =
                    if (filepath != null) DrawingSavedUiOperation(drawing, filepath)
                    else ShowErrorUiOperation(error!!)

                mUiState.value = EditorUiState(
                    pendingOperations = TakeQueue(newOperation)
                )

                // todo: setting mDrawing..

                Unit
            }
    }

    @Test
    fun clickingSaveWithExistingDrawingSetLeadsToShowingSuccessfulMessageTest() {
        val filepath = String()
        val fileUri = Uri.parse("file:///com.something")

        mockSaveCurrentDrawingChanges(filepath = filepath)

        val drawing = DrawingGeneratorUtil.generateCubeDrawing(fileUri)

        setModelDrawingBySetDrawingStateOperation(drawing)

        Espresso.onView(withId(R.id.editor_top_bar_save))
            .perform(WaitViewAction(1000), ViewActions.click())
        Espresso.onView(withText(R.string.fragment_editor_message_saved_image))
            .inRoot(withDecorView(ToastRootMatcher(mFragment.requireActivity())))
            .check(ViewAssertions.matches(isDisplayed()))
    }

    private fun mockSaveNewDrawing(
        filepath: String? = null,
        uri: Uri = Uri.parse(""),
        error: Error? = null
    ) {
        Mockito.`when`(mModel.saveCurrentDrawingToNewFile(anyObject(), Mockito.anyString()))
            .thenAnswer {
                val drawing = it.arguments[0] as Drawing
                val newOperation =
                    if (filepath != null) DrawingSavedUiOperation(drawing, filepath)
                    else ShowErrorUiOperation(error!!)

                val prevDrawing = mModel.drawing!!//mUiState.value?.drawing!!
                val newDrawing = DrawingGeneratorUtil.generateDrawingByVerticesFaces(
                    uri, prevDrawing.vertexArray, prevDrawing.faceArray
                )

                setModelDrawing(newDrawing)

                mUiState.value = EditorUiState(
                    pendingOperations = TakeQueue(newOperation)
                )

                Unit
            }
    }

    @Test
    fun clickingSaveWithNewDrawingSetLeadsToShowingFilenameInputDialogThenShowingSuccessMessageTest() {
        val filename = "something"
        val filepath = String()

        mockSaveNewDrawing(filepath = filepath)

        val drawing = DrawingGeneratorUtil.generateCubeDrawing()

        setModelDrawingBySetDrawingStateOperation(drawing)

        Espresso.onView(withId(R.id.editor_top_bar_save))
            .perform(WaitViewAction(1000), ViewActions.click())
        Espresso.onView(withText(R.string.component_dialog_filename_input_header_text))
            .check(ViewAssertions.matches(isDisplayed()))

        Espresso.onView(withId(R.id.component_dialog_filename_text_field))
            .perform(ViewActions.typeText(filename))
        Espresso.onView(withText(R.string.component_dialog_filename_input_button_positive_caption))
            .perform(ViewActions.click())

        Espresso.onView(withText(R.string.fragment_editor_message_saved_image))
            .inRoot(withDecorView(ToastRootMatcher(mFragment.requireActivity())))
            .check(ViewAssertions.matches(isDisplayed()))
    }

    private fun getColorByMaterialAttribute(@AttrRes colorAttrResId: Int): Int {
        val typedValue = TypedValue()

        Assert.assertTrue(
            mFragment.requireContext().theme.resolveAttribute(
                colorAttrResId, typedValue, true)
        )

        return typedValue.data
    }

    @Test
    fun clickingCreateFaceLeadsToEditorModeChangeTest() {
        Espresso.onView(withId(R.id.fragment_editor_button_main_action))
            .perform(ViewActions.click(), WaitViewAction(1000))

        Espresso.onView(withId(R.id.editor_bottom_bar_undo_vertex))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.editor_bottom_bar_cancel))
            .check(ViewAssertions.matches(isDisplayed()))

        Espresso.onView(withId(R.id.drawing_bottom_bar_pick_color))
            .check(ViewAssertions.doesNotExist())
        Espresso.onView(withId(R.id.editor_bottom_bar_undo_face))
            .check(ViewAssertions.doesNotExist())

        val iconTint = getColorByMaterialAttribute(
            com.google.android.material.R.attr.colorOnPrimaryContainer)

        Espresso.onView(withId(R.id.fragment_editor_button_main_action))
            .check(ViewAssertions.matches(
                FABIconDrawableViewMatcher(R.drawable.ic_check, iconTint)
            ))
    }

    @Test
    fun clickingCancelDuringAddingNewFaceLeadsToSwitchToIdleEditorModeTest() {
        Espresso.onView(withId(R.id.fragment_editor_button_main_action))
            .perform(ViewActions.click())
        Espresso.onView(withId(R.id.editor_bottom_bar_cancel))
            .perform(ViewActions.click())

        Espresso.onView(withId(R.id.drawing_bottom_bar_pick_color))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.editor_bottom_bar_undo_face))
            .check(ViewAssertions.matches(isDisplayed()))

        val iconTint = getColorByMaterialAttribute(
            com.google.android.material.R.attr.colorOnPrimaryContainer)

        Espresso.onView(withId(R.id.fragment_editor_button_main_action))
            .check(ViewAssertions.matches(
                FABIconDrawableViewMatcher(R.drawable.ic_surface, iconTint)
            ))
    }

    // todo: anything else?
}