package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing

import android.Manifest
import android.net.Uri
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
    }

    protected lateinit var mCanvasView: CanvasViewType
    protected lateinit var mTopMenuBar: Toolbar
    protected lateinit var mProgressIndicator: LinearProgressIndicator

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

        mModel.lastCameraData?.apply {
            Log.d(TAG, "onResume(): cameraData.pos = ${position.joinToString()}")

            mCanvasView.setCameraData(this@apply)
        }
        mModel.drawingSettings?.apply {
            mCanvasView.setDrawingSettings(this@apply)
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause(): entering..")

        if (mModel.uiState.value?.drawing != null)
            mModel.setLastCameraData(mCanvasView.getCameraData())

        mModel.setDrawingSettings(mCanvasView.getDrawingSettings())

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
        Log.d(TAG, "setUiElementsState(): entering..")

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