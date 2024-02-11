package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw._common.error.ErrorEnum
import com.qubacy.moveanddraw._common.util.color.ColorUtil
import com.qubacy.moveanddraw._common.util.context.getFileNameByUri
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.ui.application.activity.MainActivity
import com.qubacy.moveanddraw.ui.application.activity.file.picker.GetFileUriCallback
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.base.BaseFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.GLContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.camera._common.CameraData
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.mapper.DrawingGLDrawingMapperImpl
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.settings._common.DrawingSettings
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.view.CanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.DrawingViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.DrawingUiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.operation._common.SetDrawingUiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.operation.loaded.DrawingLoadedUiOperation
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

abstract class DrawingFragment<
    DrawingUiStateType : DrawingUiState,
    DrawingViewModelType : DrawingViewModel<DrawingUiStateType>,
    CanvasViewType : CanvasView
>(

) : BaseFragment<DrawingUiStateType, DrawingViewModelType>(), GetFileUriCallback {
    companion object {
        const val TAG = "DRAWING_FRAGMENT"

        const val LAST_CAMERA_DATA_KEY = "lastCameraData"
        const val DRAWING_SETTINGS_KEY = "drawingSettings"
    }

    protected lateinit var mCanvasView: CanvasViewType
    protected lateinit var mTopMenuBar: Toolbar
    protected lateinit var mBottomMenuBar: Toolbar
    protected lateinit var mProgressIndicator: LinearProgressIndicator

    protected var mLastCameraData: CameraData? = null
    private var mDrawingSettings: DrawingSettings? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(LAST_CAMERA_DATA_KEY, mLastCameraData)
        outState.putParcelable(DRAWING_SETTINGS_KEY, mDrawingSettings)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState == null) return

        mLastCameraData =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                savedInstanceState.getParcelable<CameraData>(LAST_CAMERA_DATA_KEY)
            else
                savedInstanceState.getParcelable(LAST_CAMERA_DATA_KEY, CameraData::class.java)
        mDrawingSettings =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                savedInstanceState.getParcelable<DrawingSettings>(DRAWING_SETTINGS_KEY)
            else
                savedInstanceState.getParcelable(DRAWING_SETTINGS_KEY, DrawingSettings::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated(): entering..")

        super.onViewCreated(view, savedInstanceState)

        inflateTopAppBarMenu(requireActivity().menuInflater, mTopMenuBar.menu)
        inflateBottomAppBarMenu(requireActivity().menuInflater, mBottomMenuBar.menu)

        mTopMenuBar.setNavigationOnClickListener { onNavigationBackButtonClicked() }
        mTopMenuBar.setOnMenuItemClickListener { onTopMenuItemClickListener(it) }
        mBottomMenuBar.setOnMenuItemClickListener { onBottomMenuItemClickListener(it) }

        mCanvasView.apply {
            init()
            setDrawingMapper(DrawingGLDrawingMapperImpl())
            setLifecycleOwner(lifecycle)
        }
    }

    override fun onResume() {
        super.onResume()

        setCurrentDrawing(mModel.drawing)

        mLastCameraData?.also {
            Log.d(TAG, "onResume(): mLastCameraData.pos = ${mLastCameraData?.position?.joinToString()}")

            mCanvasView.setCameraData(it)
        }
        mDrawingSettings?.also { setDrawingSettings(it) }
    }

    override fun onPause() {
        Log.d(TAG, "onPause(): entering..")

        mLastCameraData = mCanvasView.getCameraData()
        mDrawingSettings = mCanvasView.getDrawingSettings()

        mCanvasView.prepareForPreservation()

        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate(): entering..")

        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy(): entering..")

        super.onDestroy()
    }

    override fun setSystemBarsColor() {
        setSystemBarsColorByAttr(com.google.android.material.R.attr.colorSurfaceContainer)
    }

    protected open fun setDrawingSettings(drawingSettings: DrawingSettings) {
        mCanvasView.setDrawingSettings(drawingSettings)

        val colorInt = ColorUtil.toRGBA(
            drawingSettings.modelColor[0],
            drawingSettings.modelColor[1],
            drawingSettings.modelColor[2],
            drawingSettings.modelColor[3]
        )

        changePreviewColor(colorInt)
        setDrawingModeActionAppearanceByDrawingMode(drawingSettings.drawingMode)
    }

    protected fun setCameraData(cameraData: CameraData) {
        mCanvasView.setCameraData(cameraData)
    }

    protected open fun inflateTopAppBarMenu(menuInflater: MenuInflater, menu: Menu) {
        menuInflater.inflate(R.menu.drawing_top_bar, menu)
    }

    protected open fun inflateBottomAppBarMenu(menuInflater: MenuInflater, menu: Menu) {
        menuInflater.inflate(R.menu.drawing_bottom_bar, menu)
    }

    protected fun onTopMenuItemClickListener(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.drawing_top_bar_share -> { onShareMenuItemClicked() }
            R.id.drawing_top_bar_load -> { onLoadMenuItemClicked() }
            else -> return processCustomTopMenuAction(menuItem.itemId)
        }

        return true
    }

    protected open fun processCustomTopMenuAction(menuItemId: Int): Boolean {
        return false
    }

    protected fun onBottomMenuItemClickListener(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.drawing_bottom_bar_pick_color -> { onPickColorClicked() }
            R.id.drawing_bottom_bar_drawing_mode -> { onDrawingModeClicked() }
            else -> return processCustomBottomMenuAction(menuItem)
        }

        return true
    }

    protected open fun processCustomBottomMenuAction(menuItem: MenuItem): Boolean {
        return false
    }

    protected abstract fun onShareMenuItemClicked()

    protected open fun onLoadMenuItemClicked() {
        //requestPermissions { // todo: think of getting the permissions in an on-demand manner;
        (requireActivity() as MainActivity).chooseLocalFile(callback = this)
        //}
    }

    protected fun onNavigationBackButtonClicked() {
        findNavController().navigateUp()
    }

    protected fun shareDrawingByUri(drawingUri: Uri) {
        (requireActivity() as MainActivity).shareLocalFile(
            drawingUri, DrawingViewModel.DRAWING_MIME_TYPE
        )
    }

    override fun setUiElementsState(uiState: DrawingUiStateType) {
        Log.d(TAG, "setUiElementsState(): uiState.isLoading = ${uiState.isLoading};")

        setTopBarMenuEnabled(!uiState.isLoading)
        setProgressIndicatorEnabled(uiState.isLoading)
    }

    override fun processUiOperation(uiOperation: UiOperation) {
        if (uiOperation is SetDrawingUiOperation) {
            processSetDrawingTypeOperation(uiOperation)
        } else {
            processOtherOperation(uiOperation)
        }
    }

    protected open fun processOtherOperation(uiOperation: UiOperation) {

    }

    protected fun processSetDrawingTypeOperation(uiOperation: SetDrawingUiOperation) {
        when (uiOperation::class) {
            SetDrawingUiOperation::class -> {
                processSetDrawingOperation(uiOperation)
            }
            DrawingLoadedUiOperation::class -> {
                processDrawingLoadedOperation(uiOperation as DrawingLoadedUiOperation)
            }
            else -> processOtherSetDrawingOperation(uiOperation)
        }
    }

    protected open fun processSetDrawingOperation(uiOperation: SetDrawingUiOperation) {
        setCurrentDrawing(uiOperation.drawing)
    }

    protected open fun processDrawingLoadedOperation(uiOperation: DrawingLoadedUiOperation) {
        processSetDrawingOperation(uiOperation)

        // anything else?
    }

    protected open fun processOtherSetDrawingOperation(uiOperation: SetDrawingUiOperation) {

    }

    protected open fun setCurrentDrawing(drawing: Drawing?) {
        if (drawing == null) return

        setCanvasDrawing(drawing)
    }

    protected fun setTopBarMenuEnabled(isEnabled: Boolean) {
        mTopMenuBar.menu.setGroupEnabled(R.id.drawing_top_bar_main_group, isEnabled)
    }

    protected fun setProgressIndicatorEnabled(isEnabled: Boolean) {
        Log.d(TAG, "setProgressIndicatorEnabled(): isEnabled = $isEnabled;")

        mProgressIndicator.visibility = if (isEnabled) View.VISIBLE else View.GONE
    }

    protected open fun setCanvasDrawing(drawing: Drawing) {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d(TAG, "setCanvasDrawing(): entering..")

            mCanvasView.setFigure(drawing)
        }
    }

    override fun getPermissionsToRequest(): Array<String>? {
        return arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    override fun onFileUriGotten(fileUri: Uri?) {
        if (fileUri == null) return

        val fileName = requireContext().getFileNameByUri(fileUri)
        val ext = fileName.split('.').last()

        if (!mModel.isDrawingFileExtensionValid(ext))
            return mModel.retrieveError(ErrorEnum.WRONG_FILE_TYPE.id)

        mModel.loadDrawing(fileUri)
    }

    protected fun onPickColorClicked() {
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

    protected fun onColorPicked(@ColorInt color: Int) {
        applyModelColor(color)
    }

    protected fun applyModelColor(@ColorInt color: Int) {
        changePreviewColor(color)
        changeCanvasModelColor(color)
    }

    protected fun changePreviewColor(@ColorInt color: Int) {
        val drawable = mBottomMenuBar.menu.findItem(R.id.drawing_bottom_bar_pick_color)
            .icon!!

        DrawableCompat.setTint(drawable, color)
        drawable.invalidateSelf()
    }

    protected open fun changeCanvasModelColor(@ColorInt color: Int) = runBlocking {
        mCanvasView.setCanvasModelColor(color)
    }

    protected fun setDrawingMode(drawingMode: GLContext.DrawingMode) {
        setDrawingModeActionAppearanceByDrawingMode(drawingMode)

        mCanvasView.setFigureDrawingMode(drawingMode)
    }

    protected fun setDrawingModeActionAppearanceByDrawingMode(drawingMode: GLContext.DrawingMode) {
        val drawingModeDrawableId = when (drawingMode) {
            GLContext.DrawingMode.FILLED -> R.drawable.ic_square
            GLContext.DrawingMode.SKETCH -> R.drawable.ic_mesh
            GLContext.DrawingMode.OUTLINED -> R.drawable.ic_outlined_square
        }

        mBottomMenuBar.menu.findItem(R.id.drawing_bottom_bar_drawing_mode)
            .setIcon(drawingModeDrawableId)
    }

    private fun onDrawingModeClicked() {
        val curDrawingMode = mCanvasView.getDrawingSettings().drawingMode

        val newDrawingModeId = (curDrawingMode.id + 1) % GLContext.DrawingMode.values().size
        val newDrawingMode = GLContext.DrawingMode.getDrawingModeById(newDrawingModeId)

        setDrawingMode(newDrawingMode)
    }
}