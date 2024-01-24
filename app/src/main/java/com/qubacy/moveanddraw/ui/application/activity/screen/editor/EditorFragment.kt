package com.qubacy.moveanddraw.ui.application.activity.screen.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.transition.MaterialSharedAxis
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw.databinding.FragmentEditorBinding
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.transition.DefaultSharedAxisTransitionGenerator
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.DrawingFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.GLContext
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.view.EditorCanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.EditorViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.EditorViewModelFactoryQualifier
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.EditorUiState
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.operation.face.added.NewFaceAddedToDrawingUiOperation
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class EditorFragment(

) : DrawingFragment<EditorUiState, EditorViewModel, EditorCanvasView>(),
    Toolbar.OnMenuItemClickListener
{
    companion object {
        const val TAG = "EDITOR_FRAGMENT"

        const val STATE_MODEL_COLOR_KEY = "modelColor"
        const val EDITOR_MODE_KEY = "editorMode"
        const val DRAWING_MODE_KEY = "drawingMode"
    }

    enum class EditorMode(val id: Int) {
        MAIN(0), FACE(1);

        companion object {
            fun getBottomMenuModeById(id: Int): EditorMode {
                return EditorMode.values().first { it.id == id }
            }
        }
    }

    private val mArgs by navArgs<EditorFragmentArgs>()

    @Inject
    @EditorViewModelFactoryQualifier
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val mModel: EditorViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private lateinit var mBinding: FragmentEditorBinding
    private var mEditorMode: EditorMode = EditorMode.MAIN

    private var mModelColor: Int? = null
    private var mDrawingMode: GLContext.DrawingMode = GLContext.DrawingMode.FILLED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = DefaultSharedAxisTransitionGenerator.generate(
            requireContext(),
            MaterialSharedAxis.Z,
            true
        )
        returnTransition = DefaultSharedAxisTransitionGenerator.generate(
            requireContext(),
            MaterialSharedAxis.Z,
            false
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (mModelColor != null)
            outState.putInt(STATE_MODEL_COLOR_KEY, mModelColor!!)

        outState.putInt(EDITOR_MODE_KEY, mEditorMode.id)
        outState.putInt(DRAWING_MODE_KEY, mDrawingMode.id)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        mModelColor = savedInstanceState?.getInt(STATE_MODEL_COLOR_KEY)

        val editorModeId = savedInstanceState?.getInt(EDITOR_MODE_KEY) ?: EditorMode.MAIN.id
        val editorMode = EditorMode.getBottomMenuModeById(editorModeId)

        val drawingModeId = savedInstanceState?.getInt(DRAWING_MODE_KEY) ?: GLContext.DrawingMode.FILLED.id
        val drawingMode = GLContext.DrawingMode.getDrawingModeById(drawingModeId)

        applyCurrentModelColor()
        setEditorMode(editorMode)
        setDrawingMode(drawingMode)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentEditorBinding.inflate(inflater, container, false)

        mTopMenuBar = mBinding.fragmentEditorTopBar
        mCanvasView = mBinding.fragmentEditorCanvas.componentEditorCanvasField
        mProgressIndicator = mBinding.fragmentEditorProgressIndicator

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        mModel.setConstantOffsets(mArgs.xOffset, mArgs.yOffset, mArgs.zOffset)

        mBinding.fragmentEditorBottomBar.setOnMenuItemClickListener(this)
        mBinding.fragmentEditorButtonMainAction.setOnClickListener { onMainActionClicked() }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun setUiElementsState(uiState: EditorUiState) {
        super.setUiElementsState(uiState)

        // ??
    }

    override fun processUiOperation(uiOperation: UiOperation) {
        super.processUiOperation(uiOperation)

        when (uiOperation::class) {
            NewFaceAddedToDrawingUiOperation::class -> onNewFaceSaved()
        }
    }

    private fun onNewFaceSaved() {
        // todo: changing the bottom menu appearance if the operation went OK..

        setEditorMode(EditorMode.MAIN)
    }

    override fun setCanvasDrawing(drawing: Drawing) {
        lifecycleScope.launch(Dispatchers.IO) {
            mCanvasView.setFigure(drawing, mDrawingMode)
        }
    }

    private fun onMainActionClicked() {
        when (mEditorMode) {
            EditorMode.MAIN -> onAddFaceClicked()
            EditorMode.FACE -> onSaveFaceClicked()
        }
    }

    private fun onAddFaceClicked() {
        // todo: changing the bottom menu appearance..

        setEditorMode(EditorMode.FACE)
    }

    private fun onSaveFaceClicked() = lifecycleScope.launch(Dispatchers.IO) {
        val faceSketch = mCanvasView.saveAndGetFaceSketch()

        if (faceSketch == null) {
            // todo: output an error message..

            return@launch
        }

        launch(Dispatchers.Main) {
            mModel.saveFaceSketch(faceSketch)
        }
    }

    private fun setEditorMode(mode: EditorMode) {
        mEditorMode = mode

        when (mode) {
            EditorMode.MAIN -> {
                setBottomMenuMainGroupVisibility(true)
                mBinding.fragmentEditorCanvas.root.enableEditorMode(false)
            }
            EditorMode.FACE -> {
                setBottomMenuMainGroupVisibility(false)
                mBinding.fragmentEditorCanvas.root.enableEditorMode(true)
            }
        }

        setMainActionAppearanceByBottomMenuMode(mode)
    }

    private fun setDrawingMode(drawingMode: GLContext.DrawingMode) {
        mDrawingMode = drawingMode

        setDrawingModeActionAppearanceByDrawingMode(drawingMode)

        mBinding.fragmentEditorCanvas.root.setFigureDrawingMode(drawingMode)
    }

    private fun setDrawingModeActionAppearanceByDrawingMode(drawingMode: GLContext.DrawingMode) {
        val drawingModeDrawableId = when (drawingMode) {
            GLContext.DrawingMode.FILLED -> R.drawable.ic_square
            GLContext.DrawingMode.SKETCH -> R.drawable.ic_mesh
            GLContext.DrawingMode.OUTLINED -> R.drawable.ic_outlined_square
        }

        mBinding.fragmentEditorBottomBar.menu
            .findItem(R.id.editor_bottom_bar_drawing_mode).setIcon(drawingModeDrawableId)
    }

    private fun setBottomMenuMainGroupVisibility(isVisible: Boolean) {
        mBinding.fragmentEditorBottomBar.menu.setGroupVisible(R.id.editor_bottom_bar_main_group, isVisible)
        mBinding.fragmentEditorBottomBar.menu.setGroupVisible(R.id.editor_bottom_bar_face_group, !isVisible)
    }

    private fun setMainActionAppearanceByBottomMenuMode(mode: EditorMode) {
        val iconDrawableId = when (mode) {
            EditorMode.MAIN -> { R.drawable.ic_surface }
            EditorMode.FACE -> { R.drawable.ic_check }
        }
        val iconDrawable = ResourcesCompat.getDrawable(resources, iconDrawableId, requireContext().theme)

        mBinding.fragmentEditorButtonMainAction.setImageDrawable(iconDrawable)
    }

//    override fun checkSensorEventValidity(event: SensorEvent?): Boolean {
//        if (!super.checkSensorEventValidity(event)) return false
//
//        val curTime = System.currentTimeMillis()
//
//        if (mLastSensorDataTime + SENSOR_DELAY > curTime) return false
//
//        mLastSensorDataTime = curTime
//
//        return true
//    }

     override fun inflateTopAppBarMenu(menuInflater: MenuInflater, menu: Menu) {
        super.inflateTopAppBarMenu(menuInflater, menu)

        menuInflater.inflate(R.menu.editor_top_bar, menu)
    }

    override fun processCustomMenuAction(menuItemId: Int): Boolean {
        when (menuItemId) {
            R.id.editor_top_bar_save -> { onSaveMenuItemClicked() }
            else -> return false
        }

        return true
    }

    private fun onSaveMenuItemClicked() {
        // todo: checking if the drawing's file exists.
        // todo: if it doesn't then an appropriate dialog should be shown..



        getDrawingFilenameWithDialog() {

        }

        // todo: saving the file..


    }

    private fun getDrawingFilenameWithDialog(onProvided: (String) -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setView(R.layout.component_dialog_filename_input)
            .setPositiveButton(
                R.string.component_dialog_filename_input_button_positive_caption
            ) { dialog, which ->
                val textField = (dialog as AlertDialog)
                    .findViewById<TextInputEditText>(R.id.component_dialog_filename_text_field)!!

                onProvided(textField.text.toString())
            }
            .setNegativeButton(
                R.string.component_dialog_filename_input_button_negative_caption
            ) { dialog, which ->

            }
            .show()
    }

    override fun onShareMenuItemClicked() {
        // todo: if the drawing's file doesn't exist then save it..


        // todo: sharing the file..


    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item == null) return false

        when (item.groupId) {
            R.id.editor_bottom_bar_main_group -> { onMainGroupMenuItemClicked(item) }
            R.id.editor_bottom_bar_face_group -> { onFaceGroupMenuItemClicked(item) }
            else ->  onCommonMenuItemClicked(item)
        }

        return true
    }

    private fun onCommonMenuItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.editor_bottom_bar_drawing_mode -> { onDrawingModeClicked() }
            else -> throw IllegalStateException()
        }
    }

    private fun onMainGroupMenuItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.editor_bottom_bar_pick_color -> { onPickColorClicked() }
            R.id.editor_bottom_bar_undo_face -> { onUndoFaceClicked() }
        }
    }

    private fun onFaceGroupMenuItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.editor_bottom_bar_undo_vertex -> { onUndoVertexClicked() }
            R.id.editor_bottom_bar_cancel -> { onCancelClicked() }
        }
    }

    private fun onDrawingModeClicked() {
        val newDrawingModeId = (mDrawingMode.id + 1) % GLContext.DrawingMode.values().size
        val newDrawingMode = GLContext.DrawingMode.getDrawingModeById(newDrawingModeId)

        setDrawingMode(newDrawingMode)
    }

    private fun onCancelClicked() {
        // todo: cleaning a vertex buffer..



        setEditorMode(EditorMode.MAIN)
    }

    private fun onUndoVertexClicked() {
        lifecycleScope.launch(Dispatchers.IO) {
            mCanvasView.removeLastSketchVertex()
        }
    }

    private fun onPickColorClicked() {
        ColorPickerDialog.Builder(requireContext())
            .setTitle(R.string.component_dialog_color_picker_title)
            .setPositiveButton(
                R.string.component_dialog_color_picker_button_positive_caption,
                ColorEnvelopeListener { envelope, fromUser ->
                    onColorPicked(envelope.color)
                })
            .setNegativeButton(
                R.string.component_dialog_color_picker_button_negative_caption
            ) { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(false)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .show()
    }

    private fun onColorPicked(@ColorInt color: Int) {
        mModelColor = color

        applyCurrentModelColor()
    }

    private fun applyCurrentModelColor() {
        if (mModelColor == null) return

        changePreviewColor(mModelColor!!)
        changeCanvasModelColor(mModelColor!!)
    }

    private fun changePreviewColor(@ColorInt color: Int) {
        val drawable = mBinding.fragmentEditorBottomBar.menu
            .findItem(R.id.editor_bottom_bar_pick_color).icon!!

        DrawableCompat.setTint(drawable, color)
        drawable.invalidateSelf()
    }

    private fun changeCanvasModelColor(@ColorInt color: Int) = runBlocking {
        mCanvasView.setCanvasModelColor(color)
    }

    private fun onUndoFaceClicked() {
        mModel.removeLastFace()
    }
}