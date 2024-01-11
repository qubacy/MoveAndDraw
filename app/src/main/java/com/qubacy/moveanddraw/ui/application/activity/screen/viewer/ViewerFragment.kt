package com.qubacy.moveanddraw.ui.application.activity.screen.viewer

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw._common.error.ErrorEnum
import com.qubacy.moveanddraw.databinding.FragmentViewerBinding
import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing
import com.qubacy.moveanddraw.ui.application.activity.MainActivity
import com.qubacy.moveanddraw.ui.application.activity.file.picker.GetFileUriCallback
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.transition.DefaultSharedAxisTransitionGenerator
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.base.BaseFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.mapper.DrawingGLDrawingMapperImpl
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.DrawingViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.ViewerViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.ViewerViewModelFactoryQualifier
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.state.ViewerUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ViewerFragment(

) : BaseFragment<ViewerUiState, ViewerViewModel>(), GetFileUriCallback {

    @Inject
    @ViewerViewModelFactoryQualifier
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val mModel: ViewerViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private lateinit var mBinding: FragmentViewerBinding

    override val mIsAutomaticPermissionRequestEnabled = true

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentViewerBinding.inflate(inflater, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.fragmentViewerTopBar.setNavigationOnClickListener { onNavigationBackButtonClicked() }
        mBinding.fragmentViewerTopBar.setOnMenuItemClickListener { onMenuItemClickListener(it) }

        mBinding.fragmentViewerCanvas.componentCanvasField.apply {
            setDrawingMapper(DrawingGLDrawingMapperImpl())
        }
    }

    private fun onNavigationBackButtonClicked() {
        findNavController().navigateUp()
    }

    private fun onMenuItemClickListener(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.viewer_top_bar_share -> { onShareMenuItemClicked() }
            R.id.viewer_top_bar_load -> { onLoadMenuItemClicked() }
            else -> return false
        }

        return true
    }

    private fun onShareMenuItemClicked() {
        val drawingUri = mModel.uiState.value?.drawing?.uri

        if (drawingUri == null)
            return mModel.retrieveError(ErrorEnum.NO_FILE_LOADED.id)

        (requireActivity() as MainActivity).shareLocalFile(
            drawingUri, DrawingViewModel.DRAWING_MIME_TYPE)
    }

    private fun onLoadMenuItemClicked() {
        //requestPermissions { // todo: think of getting the permissions in an on-demand manner;
            (requireActivity() as MainActivity).chooseLocalFile(callback = this)
        //}
    }

    override fun setUiElementsState(uiState: ViewerUiState) {
        setTopBarMenuEnabled(!uiState.isLoading)
        setCurrentDrawing(uiState.drawing)
        setProgressIndicatorEnabled(uiState.isLoading)
    }

    private fun setTopBarMenuEnabled(isEnabled: Boolean) {
        mBinding.fragmentViewerTopBar.menu
            .setGroupEnabled(R.id.viewer_top_bar_main_group, isEnabled)
    }

    private fun setCurrentDrawing(drawing: Drawing?) {
        if (drawing == null) return

        setEntryMessageEnabled(false)

        lifecycleScope.launch(Dispatchers.IO) {
            mBinding.fragmentViewerCanvas.componentCanvasField.setFigure(drawing)
        }
    }

    private fun setProgressIndicatorEnabled(isEnabled: Boolean) {
        mBinding.fragmentViewerProgressIndicator.visibility =
            if (isEnabled) View.VISIBLE else View.GONE
    }

    private fun setEntryMessageEnabled(isEnabled: Boolean) {
        mBinding.fragmentViewerEntryMessage.visibility =
            if (isEnabled) View.VISIBLE else View.GONE
        mBinding.fragmentViewerEntryIcon.visibility =
            if (isEnabled) View.VISIBLE else View.GONE
    }

    override fun getPermissionsToRequest(): Array<String>? {
        return arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    override fun onFileUriGotten(fileUri: Uri?) {
        if (fileUri == null) return

        val ext = fileUri.lastPathSegment!!.split('.').last()

        if (!mModel.isDrawingFileExtensionValid(ext))
            return mModel.retrieveError(ErrorEnum.WRONG_FILE_TYPE.id)

        mModel.loadDrawing(fileUri)
    }
}