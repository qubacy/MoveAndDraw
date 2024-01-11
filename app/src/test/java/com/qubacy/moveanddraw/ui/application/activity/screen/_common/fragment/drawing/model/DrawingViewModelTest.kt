package com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing.model

import app.cash.turbine.test
import com.qubacy.moveanddraw._common.data.InitData
import com.qubacy.moveanddraw._common.error.Error
import com.qubacy.moveanddraw._common.util.mock.AnyMockUtil
import com.qubacy.moveanddraw._common.util.mock.UriMockUtil
import com.qubacy.moveanddraw._common.util.rule.MainCoroutineRule
import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing
import com.qubacy.moveanddraw.domain._common.usecase.drawing.DrawingUseCase
import com.qubacy.moveanddraw.domain._common.usecase.drawing.result.LoadDrawingResult
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment._common.model.BusinessViewModelTest
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing.model.data.DrawingMockUseCaseInitData
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.DrawingViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.DrawingUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

abstract class DrawingViewModelTest<
    UiStateType : DrawingUiState,
    UseCaseType : DrawingUseCase,
    ViewModelType : DrawingViewModel<UiStateType>
> : BusinessViewModelTest<UiStateType, UseCaseType, ViewModelType>() {
    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    override val mainCoroutineRule = MainCoroutineRule(StandardTestDispatcher())

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun initViewModel(
        error: Error?,
        useCaseMockInitData: InitData?
    ) {
        super.initViewModel(error, useCaseMockInitData)

        val drawingUseCaseMockInitData = useCaseMockInitData as DrawingMockUseCaseInitData

        Mockito.`when`(mUseCaseMock.loadDrawing(AnyMockUtil.anyObject()))
            .thenAnswer {
                mainCoroutineRule.launch {
                    System.out.println("emit(): thread.id = ${Thread.currentThread().id}")

                    mResultFlow.emit(LoadDrawingResult(drawingUseCaseMockInitData.loadedDrawing))
                }
            }
    }

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun loadDrawingTest() = runTest {
        val drawingToLoadUri = UriMockUtil.getMockedUri()
        val loadedDrawing = Drawing(floatArrayOf(), floatArrayOf(), floatArrayOf(), arrayOf())

        initViewModel(useCaseMockInitData = DrawingMockUseCaseInitData(loadedDrawing))

        mViewModel.uiStateFlow.test {
            skipItems(1)

            mViewModel.loadDrawing(drawingToLoadUri)

            val drawingState = awaitItem()!!

            Assert.assertEquals(loadedDrawing, drawingState.drawing)
        }
    }
}