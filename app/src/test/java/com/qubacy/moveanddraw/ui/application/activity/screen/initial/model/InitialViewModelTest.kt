package com.qubacy.moveanddraw.ui.application.activity.screen.initial.model

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.qubacy.moveanddraw._common._test.data.InitData
import com.qubacy.moveanddraw._common.error.Error
import com.qubacy.moveanddraw._common._test.util.mock.UriMockUtil
import com.qubacy.moveanddraw.domain.initial.InitialUseCase
import com.qubacy.moveanddraw.domain.initial.result.GetExamplePreviewsResult
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment._common.model._common.BusinessViewModelTest
import com.qubacy.moveanddraw.ui.application.activity.screen.initial.model._test.data.InitialUseCaseMockInitData
import com.qubacy.moveanddraw.ui.application.activity.screen.initial.model.state.InitialUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
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
            useCaseMockInitData as InitialUseCaseMockInitData

            Mockito.`when`(mUseCaseMock.getExamplePreviews())
                .thenAnswer {
                    mainCoroutineRule.launch {
                        mResultFlow.emit(GetExamplePreviewsResult(useCaseMockInitData.previewUris))
                    }
                }
        }
    }

    override fun mockUseCase(initData: InitData?): InitialUseCase {
        return Mockito.mock(InitialUseCase::class.java)
    }

    override fun createViewModel(
        savedStateHandleMock: SavedStateHandle,
        useCaseMock: InitialUseCase
    ): InitialViewModel {
        return InitialViewModel(savedStateHandleMock, useCaseMock)
    }

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun getExampleDrawingPreviewsTest() = runTest {
        val exampleDrawingPreviewUris = listOf(UriMockUtil.getMockedUri())

        initViewModel(useCaseMockInitData = InitialUseCaseMockInitData(exampleDrawingPreviewUris))

        mViewModel.uiStateFlow.test {
            mViewModel.getExampleDrawingPreviews()

            val uiState = awaitItem()!!

            Assert.assertEquals(exampleDrawingPreviewUris, uiState.previewUris)
        }
    }
}