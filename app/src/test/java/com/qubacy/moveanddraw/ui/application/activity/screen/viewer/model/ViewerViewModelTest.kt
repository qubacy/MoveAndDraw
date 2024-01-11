package com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model

import com.qubacy.moveanddraw._common.data.InitData
import com.qubacy.moveanddraw.domain.viewer.ViewerUseCase
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing.model.DrawingViewModelTest
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.state.ViewerUiState
import org.mockito.Mockito

class ViewerViewModelTest(

) : DrawingViewModelTest<ViewerUiState, ViewerUseCase, ViewerViewModel>() {
    override fun mockUseCase(initData: InitData?): ViewerUseCase {
        return Mockito.mock(ViewerUseCase::class.java)
    }

    override fun createViewModel(useCaseMock: ViewerUseCase): ViewerViewModel {
        return ViewerViewModel(useCaseMock)
    }
}