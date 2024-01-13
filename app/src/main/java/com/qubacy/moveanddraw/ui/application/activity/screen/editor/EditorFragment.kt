package com.qubacy.moveanddraw.ui.application.activity.screen.editor

import android.content.Context
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
import com.qubacy.moveanddraw.databinding.FragmentEditorBinding
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.transition.DefaultSharedAxisTransitionGenerator
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.AccelerometerFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.model._common.AccelerometerStateHolder
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.DrawingFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.EditorViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.EditorViewModelFactoryQualifier
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.EditorUiState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditorFragment(

) : DrawingFragment<EditorUiState, EditorViewModel>(),
    AccelerometerFragment<EditorUiState, EditorViewModel>
{
    companion object {
        const val TAG = "EDITOR_FRAGMENT"
    }

    @Inject
    @EditorViewModelFactoryQualifier
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val mModel: EditorViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private lateinit var mBinding: FragmentEditorBinding

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
        mBinding = FragmentEditorBinding.inflate(inflater, container, false)

        mTopMenuBar = mBinding.fragmentEditorTopBar
        mCanvasView = mBinding.fragmentEditorCanvas.componentCanvasField
        mProgressIndicator = mBinding.fragmentEditorProgressIndicator

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun inflateTopAppBarMenu(menuInflater: MenuInflater, menu: Menu) {
        super.inflateTopAppBarMenu(menuInflater, menu)

        menuInflater.inflate(R.menu.editor_top_bar, menu)
    }

    override fun getAccelerometerStateHolder(): AccelerometerStateHolder {
        return mModel
    }

    override fun getAccelerometerModel(): EditorViewModel {
        return mModel
    }

    override fun onShareMenuItemClicked() {
        // todo: implement..


    }

    override fun getFragmentContext(): Context {
        return requireContext()
    }
}