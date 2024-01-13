package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing

import android.Manifest
import android.net.Uri
import android.os.Bundle
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
import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing
import com.qubacy.moveanddraw.ui.application.activity.MainActivity
import com.qubacy.moveanddraw.ui.application.activity.file.picker.GetFileUriCallback
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.base.BaseFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.mapper.DrawingGLDrawingMapperImpl
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.view.CanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.DrawingViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.DrawingUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class DrawingFragment<
    DrawingUiStateType : DrawingUiState,
    DrawingViewModelType : DrawingViewModel<DrawingUiStateType>
>(

) : BaseFragment<DrawingUiStateType, DrawingViewModelType>(), GetFileUriCallback {

    protected lateinit var mCanvasView: CanvasView
    protected lateinit var mTopMenuBar: Toolbar
    protected lateinit var mProgressIndicator: LinearProgressIndicator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inflateTopAppBarMenu(requireActivity().menuInflater, mTopMenuBar.menu)

        mTopMenuBar.setNavigationOnClickListener { onNavigationBackButtonClicked() }
        mTopMenuBar.setOnMenuItemClickListener { onMenuItemClickListener(it) }

        mCanvasView.apply {
            setDrawingMapper(DrawingGLDrawingMapperImpl())
        }
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
        setTopBarMenuEnabled(!uiState.isLoading)
        setProgressIndicatorEnabled(uiState.isLoading)
    }

    protected fun setTopBarMenuEnabled(isEnabled: Boolean) {
        mTopMenuBar.menu.setGroupEnabled(R.id.drawing_top_bar_main_group, isEnabled)
    }

    protected fun setProgressIndicatorEnabled(isEnabled: Boolean) {
        mProgressIndicator.visibility = if (isEnabled) View.VISIBLE else View.GONE
    }

    protected fun setCanvasDrawing(drawing: Drawing) {
        lifecycleScope.launch(Dispatchers.IO) {
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