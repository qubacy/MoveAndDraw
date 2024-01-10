package com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing.model

import com.qubacy.moveanddraw._common.data.InitData
import com.qubacy.moveanddraw._common.error.Error
import com.qubacy.moveanddraw._common.util.livedata.getOrAwaitValue
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadDrawingTest() = runTest {
        System.out.println("loadDrawingTest(): thread.id = ${Thread.currentThread().id}")

        val drawingToLoadUri = UriMockUtil.getMockedUri()
        val loadedDrawing = Drawing(floatArrayOf(), floatArrayOf(), floatArrayOf(), arrayOf())

        initViewModel(useCaseMockInitData = DrawingMockUseCaseInitData(loadedDrawing))

        mViewModel.loadDrawing(drawingToLoadUri)

        // todo: DOESN'T WORK!! it doesn't return THE LAST UI STATE (because of .emit()):

        val scheduler = (mainCoroutineRule.coroutineDispatcher as TestDispatcher).scheduler

        scheduler.runCurrent()

        val uiState = mViewModel.uiState.getOrAwaitValue()!!

        System.out.println("loadDrawingTest(): ending..")

        Assert.assertEquals(loadedDrawing, uiState.drawing)
    }
}