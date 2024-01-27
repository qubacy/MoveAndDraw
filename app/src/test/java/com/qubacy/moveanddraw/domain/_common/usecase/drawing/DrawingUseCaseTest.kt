package com.qubacy.moveanddraw.domain._common.usecase.drawing

import app.cash.turbine.test
import com.qubacy.moveanddraw._common.util.mock.AnyMockUtil
import com.qubacy.moveanddraw._common.util.mock.UriMockUtil
import com.qubacy.moveanddraw._common.util.rule.MainCoroutineRule
import com.qubacy.moveanddraw.data.drawing.model.DataDrawing
import com.qubacy.moveanddraw.data.drawing.model.toDrawing
import com.qubacy.moveanddraw.data.drawing.repository.DrawingDataRepository
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw._common.data.InitData
import com.qubacy.moveanddraw.domain._common.usecase.drawing.result.LoadDrawingResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

abstract class DrawingUseCaseTest<UseCaseType : DrawingUseCase> {
    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(Dispatchers.IO)

    protected lateinit var mDrawingUseCase: DrawingUseCase

    protected abstract fun generateDrawingUseCase(
        errorDataRepositoryMock: ErrorDataRepository,
        drawingDataRepositoryMock: DrawingDataRepository,
        initData: InitData? = null
    ): UseCaseType

    @OptIn(ExperimentalCoroutinesApi::class)
    protected fun initUseCase(
        initData: InitData? = null,
        loadedDataDrawing: DataDrawing =
            DataDrawing(floatArrayOf(), floatArrayOf(), floatArrayOf(), arrayOf()),
        coroutineScope: CoroutineScope = GlobalScope,
        coroutineDispatcher: CoroutineDispatcher = mainCoroutineRule.coroutineDispatcher
    ) {
        val errorDataRepositoryMock = Mockito.mock(ErrorDataRepository::class.java)

        val drawingDataRepositoryMock = Mockito.mock(DrawingDataRepository::class.java)

        Mockito.`when`(drawingDataRepositoryMock.loadDrawing(
            AnyMockUtil.anyObject()
        ))
            .thenReturn(loadedDataDrawing)

        mDrawingUseCase = generateDrawingUseCase(
            errorDataRepositoryMock, drawingDataRepositoryMock, initData
        ).apply {
            setCoroutineScope(coroutineScope)
            setCoroutineDispatcher(coroutineDispatcher)
        }
    }

    @Before
    fun setup() {

    }

    @Test
    fun loadDrawingTest(): Unit = runTest {
        val mockedUri = UriMockUtil.getMockedUri()
        val loadedDataDrawing = DataDrawing(floatArrayOf(), floatArrayOf(), floatArrayOf(), arrayOf())
        val loadedDrawing = loadedDataDrawing.toDrawing(mockedUri)

        initUseCase(loadedDataDrawing = loadedDataDrawing)

        mDrawingUseCase.resultFlow.test {
            skipItems(1)
            mDrawingUseCase.loadDrawing(mockedUri)

            val result = awaitItem()!!

            Assert.assertEquals(LoadDrawingResult::class, result::class)
            Assert.assertEquals(loadedDrawing, (result as LoadDrawingResult).drawing)
        }
    }
}