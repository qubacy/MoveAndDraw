package com.qubacy.moveanddraw.ui.application.activity.screen.editor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.transition.MaterialSharedAxis
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw._common.error.ErrorEnum
import com.qubacy.moveanddraw.databinding.FragmentEditorBinding
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.transition.DefaultSharedAxisTransitionGenerator
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.DrawingFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.operation._common.SetDrawingUiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas._common.EditorCanvasContext
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.view.EditorCanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.EditorViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.EditorViewModelFactoryQualifier
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.EditorUiState
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.operation.face.added.NewFaceAddedToDrawingUiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.operation.saved.DrawingSavedUiOperation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EditorFragment(

) : DrawingFragment<EditorUiState, EditorViewModel, EditorCanvasView>()
    //Toolbar.OnMenuItemClickListener
{
    companion object {
        const val TAG = "EDITOR_FRAGMENT"

        const val EDITOR_MODE_KEY = "editorMode"
    }

    @Inject
    @EditorViewModelFactoryQualifier
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val mModel: EditorViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private lateinit var mBinding: FragmentEditorBinding

    private var mEditorMode: EditorCanvasContext.Mode? = null

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

        outState.putShort(EDITOR_MODE_KEY, mCanvasView.getEditorMode().id) // todo: is it ok?
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState == null) return

        val editorModeId = savedInstanceState.getShort(EDITOR_MODE_KEY)
        val editorMode = EditorCanvasContext.Mode.getModeById(editorModeId)

        setEditorMode(editorMode)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentEditorBinding.inflate(inflater, container, false)

        mTopMenuBar = mBinding.fragmentEditorTopBar
        mBottomMenuBar = mBinding.fragmentEditorBottomBar
        mCanvasView = mBinding.fragmentEditorCanvas.componentEditorCanvasField
        mProgressIndicator = mBinding.fragmentEditorProgressIndicator

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.fragmentEditorButtonMainAction.setOnClickListener { onMainActionClicked() }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onPause() {
        mModel.setFaceSketchDotBuffer(mCanvasView.getFaceSketchDotBuffer())

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        mEditorMode?.also { mCanvasView.setEditorMode(it, true) }
        mModel.faceSketchDotBuffer?.also {
            Log.d(TAG, "onResume(): faceSketchDotBuffer = ${it.joinToString()}")

            mCanvasView.setFaceSketchDotBuffer(it)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")

        super.onDestroy()
    }

    override fun setUiElementsState(uiState: EditorUiState) {
        super.setUiElementsState(uiState)

        // ??
    }

    override fun processOtherSetDrawingOperation(uiOperation: SetDrawingUiOperation) {
        super.processOtherSetDrawingOperation(uiOperation)

        when (uiOperation::class) {
            NewFaceAddedToDrawingUiOperation::class ->
                processNewFaceAddedToDrawingOperation(uiOperation as NewFaceAddedToDrawingUiOperation)
            DrawingSavedUiOperation::class ->
                onDrawingSaved(uiOperation as DrawingSavedUiOperation)
        }
    }

    private fun processNewFaceAddedToDrawingOperation(
        uiOperation: NewFaceAddedToDrawingUiOperation
    ) {
        setCurrentDrawing(uiOperation.drawing)
        onNewFaceSaved()
    }

    private fun onNewFaceSaved() {
        setEditorMode(EditorCanvasContext.Mode.VIEWING)
    }

    private fun onDrawingSaved(operation: DrawingSavedUiOperation) {
        onMessageOccurred(R.string.fragment_editor_message_saved_image)

        if (mModel.getIsSharingPending()) onSharingDrawingSaved()
    }

    private fun onSharingDrawingSaved() {
        val curDrawing = mModel.drawing!!

        shareDrawingByUri(curDrawing.uri!!)

        mModel.setIsSharingPending(false)
    }

    private fun onMainActionClicked() {
        when (mCanvasView.getEditorMode()) {
            EditorCanvasContext.Mode.VIEWING -> onAddFaceClicked()
            EditorCanvasContext.Mode.CREATING_FACE -> onSaveFaceClicked()
        }
    }

    private fun onAddFaceClicked() {
        setEditorMode(EditorCanvasContext.Mode.CREATING_FACE)
    }

    private fun onSaveFaceClicked() = lifecycleScope.launch(Dispatchers.IO) {
        val faceSketch = mCanvasView.saveAndGetFaceSketch()

        if (faceSketch == null) {
            mModel.retrieveError(ErrorEnum.NULL_FACE_SKETCH.id)

            return@launch
        }

        launch(Dispatchers.Main) {
            val drawing = mModel.drawing

            mModel.saveFaceSketch(drawing, faceSketch)
        }
    }

    private fun setEditorMode(editorMode: EditorCanvasContext.Mode) {
        mEditorMode = editorMode

        when (editorMode) {
            EditorCanvasContext.Mode.VIEWING -> {
                setBottomMenuMainGroupVisibility(true)
            }
            EditorCanvasContext.Mode.CREATING_FACE -> {
                setBottomMenuMainGroupVisibility(false)
            }
        }

        mCanvasView.setEditorMode(editorMode)
        setMainActionAppearanceByBottomMenuMode(editorMode)
    }

    private fun setBottomMenuMainGroupVisibility(isVisible: Boolean) {
        mBinding.fragmentEditorBottomBar.menu.findItem(R.id.drawing_bottom_bar_pick_color)
            .isVisible = isVisible
        mBinding.fragmentEditorBottomBar.menu.setGroupVisible(
            R.id.editor_bottom_bar_main_group, isVisible)
        mBinding.fragmentEditorBottomBar.menu.setGroupVisible(
            R.id.editor_bottom_bar_face_group, !isVisible)
    }

    private fun setMainActionAppearanceByBottomMenuMode(editorMode: EditorCanvasContext.Mode) {
        val iconDrawableId = when (editorMode) {
            EditorCanvasContext.Mode.VIEWING -> { R.drawable.ic_surface }
            EditorCanvasContext.Mode.CREATING_FACE -> { R.drawable.ic_check }
        }
        val iconDrawable = ResourcesCompat.getDrawable(resources, iconDrawableId, requireContext().theme)

        mBinding.fragmentEditorButtonMainAction.setImageDrawable(iconDrawable)
    }

     override fun inflateTopAppBarMenu(menuInflater: MenuInflater, menu: Menu) {
        super.inflateTopAppBarMenu(menuInflater, menu)

        menuInflater.inflate(R.menu.editor_top_bar, menu)
    }

    override fun inflateBottomAppBarMenu(menuInflater: MenuInflater, menu: Menu) {
        super.inflateBottomAppBarMenu(menuInflater, menu)

        menuInflater.inflate(R.menu.editor_bottom_bar, menu)
    }

    override fun processCustomTopMenuAction(menuItemId: Int): Boolean {
        when (menuItemId) {
            R.id.editor_top_bar_save -> { onSaveMenuItemClicked() }
            else -> return false
        }

        return true
    }

    private fun onSaveMenuItemClicked() {
        val curDrawing = mModel.drawing

        if (curDrawing == null) {
            mModel.retrieveError(ErrorEnum.NULL_CURRENT_DRAWING.id)

            return
        }

        if (curDrawing.uri != null)
            return saveExistingDrawingFile(curDrawing)

        saveNewDrawingFile(curDrawing)
    }

    private fun saveExistingDrawingFile(drawing: Drawing) {
        if (!mModel.checkDrawingValidity(drawing)) {
            mModel.retrieveError(ErrorEnum.INVALID_DRAWING.id)

            return
        }

        mModel.saveCurrentDrawingChanges(drawing)
    }

    private fun saveNewDrawingFile(drawing: Drawing, onEnded: (() -> Unit)? = null) {
        if (!mModel.checkDrawingValidity(drawing)) {
            mModel.retrieveError(ErrorEnum.INVALID_DRAWING.id)

            return
        }

        getDrawingFilenameWithDialog() {
            if (!mModel.checkNewFileFilenameValidity(it)) {
                mModel.retrieveError(ErrorEnum.INVALID_DRAWING_FILENAME.id)

                return@getDrawingFilenameWithDialog
            }

            mModel.saveCurrentDrawingToNewFile(drawing, it)
            onEnded?.invoke()
        }
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
        val curDrawing = mModel.drawing

        if (curDrawing == null) {
            mModel.retrieveError(ErrorEnum.NULL_CURRENT_DRAWING.id)

            return
        }

        if (curDrawing.uri == null) saveNewDrawingFile(curDrawing)
        else saveExistingDrawingFile(curDrawing)

        mModel.setIsSharingPending(true)
    }

    override fun processCustomBottomMenuAction(menuItem: MenuItem): Boolean {
        when (menuItem.groupId) {
            R.id.editor_bottom_bar_main_group -> { onMainGroupMenuItemClicked(menuItem) }
            R.id.editor_bottom_bar_face_group -> { onFaceGroupMenuItemClicked(menuItem) }
            else -> throw IllegalStateException()
        }

        return true
    }

    private fun onMainGroupMenuItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.editor_bottom_bar_undo_face -> { onUndoFaceClicked() }
        }
    }

    private fun onFaceGroupMenuItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.editor_bottom_bar_undo_vertex -> { onUndoVertexClicked() }
            R.id.editor_bottom_bar_cancel -> { onCancelClicked() }
        }
    }

    private fun onCancelClicked() {
        setEditorMode(EditorCanvasContext.Mode.VIEWING)
    }

    private fun onUndoVertexClicked() {
        lifecycleScope.launch(Dispatchers.IO) {
            mCanvasView.removeLastSketchVertex()
        }
    }

    private fun onUndoFaceClicked() {
        val drawing = mModel.drawing

        if (drawing == null) {
            mModel.retrieveError(ErrorEnum.NULL_CURRENT_DRAWING.id)

            return
        }

        mModel.removeLastFace(drawing)
    }
}