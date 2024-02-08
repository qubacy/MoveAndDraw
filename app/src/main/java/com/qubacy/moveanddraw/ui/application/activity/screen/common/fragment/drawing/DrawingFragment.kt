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
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw._common.error.ErrorEnum
import com.qubacy.moveanddraw._common.util.context.getFileNameByUri
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.ui.application.activity.MainActivity
import com.qubacy.moveanddraw.ui.application.activity.file.picker.GetFileUriCallback
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.base.BaseFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.camera._common.CameraData
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.mapper.DrawingGLDrawingMapperImpl
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.settings._common.DrawingSettings
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.view.CanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.DrawingViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.DrawingUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        mTopMenuBar.setNavigationOnClickListener { onNavigationBackButtonClicked() }
        mTopMenuBar.setOnMenuItemClickListener { onMenuItemClickListener(it) }

        mCanvasView.apply {
            init()
            setDrawingMapper(DrawingGLDrawingMapperImpl())
            setLifecycleOwner(lifecycle)
        }
    }

    override fun onResume() {
        super.onResume()

        mLastCameraData?.also { mCanvasView.setCameraData(it, true) }
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

    protected open fun setDrawingSettings(drawingSettings: DrawingSettings) {
        mCanvasView.setDrawingSettings(drawingSettings)
    }

    protected fun setCameraData(cameraData: CameraData) {
        mCanvasView.setCameraData(cameraData)
    }

    protected open fun inflateTopAppBarMenu(menuInflater: MenuInflater, menu: Menu) {
        menuInflater.inflate(R.menu.drawing_top_bar, menu)
    }

    protected fun onMenuItemClickListener(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.drawing_top_bar_share -> { onShareMenuItemClicked() }
            R.id.drawing_top_bar_load -> { onLoadMenuItemClicked() }
            else -> return processCustomMenuAction(menuItem.itemId)
        }

        return true
    }

    protected open fun processCustomMenuAction(menuItemId: Int): Boolean {
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
        setCurrentDrawing(uiState.drawing)
    }

    private fun setCurrentDrawing(drawing: Drawing?) {
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
}