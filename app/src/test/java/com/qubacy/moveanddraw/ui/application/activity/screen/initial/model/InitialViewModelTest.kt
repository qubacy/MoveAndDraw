package com.qubacy.moveanddraw.ui.application.activity.screen.initial.model

import com.qubacy.moveanddraw._common.data.InitData
import com.qubacy.moveanddraw._common.error.Error
import com.qubacy.moveanddraw._common.util.livedata.getOrAwaitValue
import com.qubacy.moveanddraw._common.util.mock.UriMockUtil
import com.qubacy.moveanddraw.domain.initial.InitialUseCase
import com.qubacy.moveanddraw.domain.initial.result.GetExamplePreviewsResult
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment._common.model.BusinessViewModelTest
import com.qubacy.moveanddraw.ui.application.activity.screen.initial.model.data.InitialUseCaseMockInitData
import com.qubacy.moveanddraw.ui.application.activity.screen.initial.model.state.InitialUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class InitialViewModelTest :
    BusinessViewModelTest<InitialUiState, InitialUseCase, InitialViewModel>() {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun initViewModel(
        error: Error?,
        useCaseMockInitData: InitData?
    ) {
        super.initViewModel(error, null)

        if (useCaseMockInitData != null) {
            val initialUseCaseMockInitData = useCaseMockInitData as InitialUseCaseMockInitData

            Mockito.`when`(mUseCaseMock.getExamplePreviews())
                .thenAnswer {
                    mainCoroutineRule.launch {
                        mResultFlow.emit(GetExamplePreviewsResult(initialUseCaseMockInitData.previewUris))
                    }
                }
        }
    }

    override fun mockUseCase(initData: InitData?): InitialUseCase {
        return Mockito.mock(InitialUseCase::class.java)
    }

    override fun createViewModel(useCaseMock: InitialUseCase): InitialViewModel {
        return InitialViewModel(useCaseMock)
    }

    @Before
    override fun setup() {
        super.setup()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getExampleDrawingPreviewsTest() = mainCoroutineRule.run {
        val exampleDrawingPreviewUris = listOf(UriMockUtil.getMockedUri())

        initViewModel(useCaseMockInitData = InitialUseCaseMockInitData(exampleDrawingPreviewUris))

        mViewModel.getExampleDrawingPreviews()

        val uiState = mViewModel.uiState.getOrAwaitValue()!!

        Assert.assertEquals(exampleDrawingPreviewUris, uiState.previewUris)
    }
}