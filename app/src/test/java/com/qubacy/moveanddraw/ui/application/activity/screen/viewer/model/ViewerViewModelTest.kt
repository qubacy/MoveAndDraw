package com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model

import androidx.lifecycle.SavedStateHandle
import com.qubacy.moveanddraw._common._test.data.InitData
import com.qubacy.moveanddraw.domain.viewer.ViewerUseCase
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing.model.DrawingViewModelTest
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.state.ViewerUiState
import org.mockito.Mockito

class ViewerViewModelTest(

) : DrawingViewModelTest<ViewerUiState, ViewerUseCase, ViewerViewModel>() {
    override fun mockUseCase(initData: InitData?): ViewerUseCase {
        return Mockito.mock(ViewerUseCase::class.java)
    }

    override fun createViewModel(
        savedStateHandle: SavedStateHandle,
        useCaseMock: ViewerUseCase
    ): ViewerViewModel {
        return ViewerViewModel(savedStateHandle, useCaseMock)
    }
}