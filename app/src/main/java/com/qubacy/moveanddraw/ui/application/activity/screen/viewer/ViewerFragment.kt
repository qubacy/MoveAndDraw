package com.qubacy.moveanddraw.ui.application.activity.screen.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw.databinding.FragmentViewerBinding
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.transition.DefaultSharedAxisTransitionGenerator
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.base.BaseFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.ViewerViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.ViewerViewModelFactoryQualifier
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.state.ViewerUiState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ViewerFragment(

) : BaseFragment<ViewerUiState, ViewerViewModel>() {

    @Inject
    @ViewerViewModelFactoryQualifier
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val mModel: ViewerViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private lateinit var mBinding: FragmentViewerBinding

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
        // todo: implement..


    }

    private fun onLoadMenuItemClicked() {
        // todo: implement..


    }

    override fun setUiElementsState(uiState: ViewerUiState) {
        TODO("Not yet implemented")
    }


}