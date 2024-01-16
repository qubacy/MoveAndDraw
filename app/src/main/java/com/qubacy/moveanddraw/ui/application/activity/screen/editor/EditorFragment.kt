package com.qubacy.moveanddraw.ui.application.activity.screen.editor

import android.content.Context
import android.hardware.SensorEvent
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
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.transition.DefaultSharedAxisTransitionGenerator
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.AccelerometerFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.model._common.AccelerometerStateHolder
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.DrawingFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.view.EditorCanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.EditorViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.EditorViewModelFactoryQualifier
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.EditorUiState
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
    AccelerometerFragment<EditorUiState, EditorViewModel>,
    Toolbar.OnMenuItemClickListener
{
    companion object {
        const val TAG = "EDITOR_FRAGMENT"

        const val STATE_MODEL_COLOR_KEY = "modelColor"
        const val SENSOR_DELAY = 100L
    }

    private val mArgs by navArgs<EditorFragmentArgs>()

    @Inject
    @EditorViewModelFactoryQualifier
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val mModel: EditorViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private lateinit var mBinding: FragmentEditorBinding

    private var mModelColor: Int? = null
    private var mLastSensorDataTime: Long = 0L

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
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        mModelColor = savedInstanceState?.getInt(STATE_MODEL_COLOR_KEY)

        applyCurrentModelColor()
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

        mModel.setConstantOffsets(mArgs.xOffset, mArgs.yOffset, mArgs.zOffset)

        mBinding.fragmentEditorBottomBar.setOnMenuItemClickListener(this)

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onStart() {
        super.onStart()

        startSensorListening()
    }

    override fun onDestroy() {
        endSensorListening()

        super.onDestroy()
    }

    override fun setUiElementsState(uiState: EditorUiState) {
        super.setUiElementsState(uiState)

        applyNewDevicePosition(uiState.devicePos)
    }

    private fun applyNewDevicePosition(devicePos: FloatArray) {
        lifecycleScope.launch (Dispatchers.IO) {
            mCanvasView.changeDevicePosition(devicePos[0], devicePos[1], devicePos[2])
        }
    }

    override fun checkSensorEventValidity(event: SensorEvent?): Boolean {
        if (!super.checkSensorEventValidity(event)) return false

        val curTime = System.currentTimeMillis()

        if (mLastSensorDataTime + SENSOR_DELAY > curTime) return false

        mLastSensorDataTime = curTime

        return true
    }

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

    override fun getAccelerometerStateHolder(): AccelerometerStateHolder {
        return mModel
    }

    override fun getAccelerometerModel(): EditorViewModel {
        return mModel
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

    override fun getFragmentContext(): Context {
        return requireContext()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item == null) return false

        when (item.itemId) {
            R.id.editor_bottom_bar_pick_color -> { onPickColorClicked() }
            R.id.editor_bottom_bar_undo -> { onUndoClicked() }
            else ->  return false
        }

        return true
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

    private fun onUndoClicked() {
        mModel.removeLastFace()
    }
}