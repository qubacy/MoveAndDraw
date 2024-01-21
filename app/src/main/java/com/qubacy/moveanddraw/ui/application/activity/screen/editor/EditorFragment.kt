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
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.transition.MaterialSharedAxis
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw.databinding.FragmentEditorBinding
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.transition.DefaultSharedAxisTransitionGenerator
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.DrawingFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.view.EditorCanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.EditorViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.EditorViewModelFactoryQualifier
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.EditorUiState
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import dagger.hilt.android.AndroidEntryPoint
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
        const val BOTTOM_MENU_MODE_KEY = "bottomMenuMode"
    }

    enum class BottomMenuMode(val id: Int) {
        MAIN(0), FACE(1);

        companion object {
            fun getBottomMenuModeById(id: Int): BottomMenuMode {
                return BottomMenuMode.values().first { it.id == id }
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
    private var mBottomMenuMode: BottomMenuMode = BottomMenuMode.MAIN

    private var mModelColor: Int? = null

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

        outState.putInt(BOTTOM_MENU_MODE_KEY, mBottomMenuMode.id)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        mModelColor = savedInstanceState?.getInt(STATE_MODEL_COLOR_KEY)

        val bottomMenuModeId = savedInstanceState?.getInt(BOTTOM_MENU_MODE_KEY) ?: BottomMenuMode.MAIN.id
        val bottomMenuMode = BottomMenuMode.getBottomMenuModeById(bottomMenuModeId)

        applyCurrentModelColor()
        setBottomMenuMode(bottomMenuMode)
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

    private fun onMainActionClicked() {
        when (mBottomMenuMode) {
            BottomMenuMode.MAIN -> onAddFaceClicked()
            BottomMenuMode.FACE -> onCancelClicked()
        }
    }

    private fun onAddFaceClicked() {
        // todo: changing the bottom menu appearance..

        setBottomMenuMode(BottomMenuMode.FACE)
    }

    private fun onSaveFaceClicked() {
        // todo: saving a new face...


        // todo: changing the bottom menu appearance if the operation went OK..

        setBottomMenuMode(BottomMenuMode.MAIN)
    }

    private fun setBottomMenuMode(mode: BottomMenuMode) {
        mBottomMenuMode = mode

        when (mode) {
            BottomMenuMode.MAIN -> { setBottomMenuMainGroupVisibility(true) }
            BottomMenuMode.FACE -> { setBottomMenuMainGroupVisibility(false) }
        }

        setMainActionAppearanceByBottomMenuMode(mode)
    }

    private fun setBottomMenuMainGroupVisibility(isVisible: Boolean) {
        mBinding.fragmentEditorBottomBar.menu.setGroupVisible(R.id.editor_bottom_bar_main_group, isVisible)
        mBinding.fragmentEditorBottomBar.menu.setGroupVisible(R.id.editor_bottom_bar_face_group, !isVisible)
    }

    private fun setMainActionAppearanceByBottomMenuMode(mode: BottomMenuMode) {
        val iconDrawableId = when (mode) {
            BottomMenuMode.MAIN -> { R.drawable.ic_surface }
            BottomMenuMode.FACE -> { R.drawable.ic_check }
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
            else ->  return false
        }

        return true
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

    private fun onCancelClicked() {
        // todo: cleaning a vertex buffer..



        setBottomMenuMode(BottomMenuMode.MAIN)
    }

    private fun onUndoVertexClicked() {
        // todo: implement..


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