package com.qubacy.moveanddraw.ui.application.activity.screen.initial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.HeroCarouselStrategy
import com.qubacy.moveanddraw.databinding.FragmentInitialBinding
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.base.BaseFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.initial.component.carousel.adapter.InitialDrawingCarouselAdapter
import com.qubacy.moveanddraw.ui.application.activity.screen.initial.model.InitialViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InitialFragment : BaseFragment() {
    private val mModel: InitialViewModel by viewModels()
    private lateinit var mBinding: FragmentInitialBinding

    private lateinit var mCarouselAdapter: InitialDrawingCarouselAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        mModel.getExampleDrawingPreviews().observe(viewLifecycleOwner) {
            mCarouselAdapter.submitList(it)
        }
    }
}