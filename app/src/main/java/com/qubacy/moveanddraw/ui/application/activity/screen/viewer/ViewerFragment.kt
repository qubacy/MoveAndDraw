package com.qubacy.moveanddraw.ui.application.activity.screen.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.transition.MaterialSharedAxis
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw._common.error.ErrorEnum
import com.qubacy.moveanddraw.databinding.FragmentViewerBinding
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.transition.DefaultSharedAxisTransitionGenerator
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.DrawingFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.view.CanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.operation.loaded.DrawingLoadedUiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.ViewerViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.ViewerViewModelFactoryQualifier
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.state.ViewerUiState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ViewerFragment(

) : DrawingFragment<ViewerUiState, ViewerViewModel, CanvasView>() {

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

        mTopMenuBar = mBinding.fragmentViewerTopBar
        mCanvasView = mBinding.fragmentViewerCanvas.componentCanvasField
        mProgressIndicator = mBinding.fragmentViewerProgressIndicator

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun inflateTopAppBarMenu(menuInflater: MenuInflater, menu: Menu) {
        super.inflateTopAppBarMenu(menuInflater, menu)

        menuInflater.inflate(R.menu.viewer_top_bar, menu)
    }

    override fun onShareMenuItemClicked() {
        val drawingUri = mModel.drawing?.uri//mModel.uiState.value?.drawing?.uri

        if (drawingUri == null)
            return mModel.retrieveError(ErrorEnum.NO_FILE_LOADED.id)

        shareDrawingByUri(drawingUri)
    }

    override fun setUiElementsState(uiState: ViewerUiState) {
        super.setUiElementsState(uiState)
    }

    override fun processDrawingLoadedOperation(uiOperation: DrawingLoadedUiOperation) {
        super.processDrawingLoadedOperation(uiOperation)

        setEntryMessageEnabled(false)
    }

    private fun setEntryMessageEnabled(isEnabled: Boolean) {
        mBinding.fragmentViewerEntryMessage.visibility =
            if (isEnabled) View.VISIBLE else View.GONE
        mBinding.fragmentViewerEntryIcon.visibility =
            if (isEnabled) View.VISIBLE else View.GONE
    }
}