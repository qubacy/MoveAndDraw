package com.qubacy.moveanddraw.ui.application.activity.screen.initial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.HeroCarouselStrategy
import com.qubacy.moveanddraw.databinding.FragmentInitialBinding
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.base.BaseFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.initial.component.carousel.adapter.InitialDrawingCarouselAdapter
import com.qubacy.moveanddraw.ui.application.activity.screen.initial.component.chooser.view.OptionChooserComponent
import com.qubacy.moveanddraw.ui.application.activity.screen.initial.component.chooser.view.OptionChooserComponentCallback
import com.qubacy.moveanddraw.ui.application.activity.screen.initial.model.InitialViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.initial.model.state.InitialUiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InitialFragment(

) : BaseFragment<InitialUiState, InitialViewModel>(), OptionChooserComponentCallback {
    companion object {
        const val TAG = "INITIAL_FRAGMENT"

        const val DEFAULT_SCRIM_FADE_DURATION = 400L
    }

    override val mModel: InitialViewModel by viewModels()
    private lateinit var mBinding: FragmentInitialBinding

    private lateinit var mCarouselAdapter: InitialDrawingCarouselAdapter

    private lateinit var mScrimCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mScrimCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() { onBackPressed() }
        }

        requireActivity().onBackPressedDispatcher.addCallback(mScrimCallback)
    }

    private fun onBackPressed() {
        changeScrimEnabled(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentInitialBinding.inflate(inflater, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mCarouselAdapter = InitialDrawingCarouselAdapter()

        mBinding.fragmentInitialDrawingCarousel.apply {
            layoutManager = CarouselLayoutManager(HeroCarouselStrategy())
            adapter = mCarouselAdapter
        }
        mBinding.fragmentInitialButtonStart.apply {
            setOnClickListener { onStartClicked() }
        }
        (mBinding.fragmentInitialOptionChooser.root as OptionChooserComponent)
            .setSwipeOptionChosenCallback(this)

        mModel.uiState.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            processUiState(it)
        }
        mModel.getExampleDrawingPreviews()
    }

    override fun setUiElementsState(uiState: InitialUiState) {
        mCarouselAdapter.submitList(uiState.previewUris)
    }

    private fun onStartClicked() {
        changeScrimEnabled(true)
    }

    private fun changeScrimEnabled(isEnabled: Boolean, endAction: (() -> Unit)? = null) {
        changeOptionChooserVisibility(isEnabled, endAction)

        mScrimCallback.isEnabled = isEnabled
    }

    private fun changeOptionChooserVisibility(isVisible: Boolean, endAction: (() -> Unit)? = null) {
        mBinding.fragmentInitialOptionChooser.root.apply {
            if (isVisible) alpha = 0f

            val animation = animate()
                .setDuration(DEFAULT_SCRIM_FADE_DURATION)
                .setInterpolator(AccelerateDecelerateInterpolator())

            if (isVisible) {
                animation.withStartAction {
                    mBinding.fragmentInitialOptionChooser.root.visibility = View.VISIBLE

                }.alpha(1f)

            } else {
                animation.withEndAction {
                    mBinding.fragmentInitialOptionChooser.root.visibility = View.GONE

                    endAction?.invoke()

                }.alpha(0f)
            }

            animation.start()
        }
    }

    override fun onSwipeOptionChosen(
        swipeOption: OptionChooserComponentCallback.SwipeOption,
        endAction: (() -> Unit)?
    ) {
        changeScrimEnabled(false, endAction)

        // todo: implement a transition..


    }

    override fun onScrimClicked() {
        onBackPressed()
    }
}