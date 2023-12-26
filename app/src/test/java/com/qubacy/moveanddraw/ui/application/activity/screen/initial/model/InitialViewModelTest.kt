package com.qubacy.moveanddraw.ui.application.activity.screen.initial.model

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.qubacy.moveanddraw._common.util.livedata.getOrAwaitValue
import com.qubacy.moveanddraw._common.util.mock.UriMockUtil
import com.qubacy.moveanddraw._common.util.rule.MainCoroutineRule
import com.qubacy.moveanddraw.domain._common.usecase.result._common.Result
import com.qubacy.moveanddraw.domain.initial.InitialUseCase
import com.qubacy.moveanddraw.domain.initial.result.GetExamplePreviewsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class InitialViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(Dispatchers.IO)

    private lateinit var mResultFlow: MutableStateFlow<Result?>

    private lateinit var mInitialViewModel: InitialViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initViewModel(
        previewUris: List<Uri> = listOf()
    ) {
        mResultFlow = MutableStateFlow(null)

        val initialUseCaseMock = Mockito.mock(InitialUseCase::class.java)

        Mockito.`when`(initialUseCaseMock.resultFlow)
            .thenReturn(mResultFlow)
        Mockito.`when`(initialUseCaseMock.getExamplePreviews())
            .thenAnswer {
                mainCoroutineRule.launch {
                    mResultFlow.emit(GetExamplePreviewsResult(previewUris))
                }
            }

        mInitialViewModel = InitialViewModel(initialUseCaseMock)
    }

    @Before
    fun setup() {

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getExampleDrawingPreviewsTest() = mainCoroutineRule.run {
        val exampleDrawingPreviewUris = listOf(UriMockUtil.getMockedUri())

        initViewModel(exampleDrawingPreviewUris)

        mInitialViewModel.getExampleDrawingPreviews()

        val uiState = mInitialViewModel.uiState.getOrAwaitValue()!!

        Assert.assertEquals(exampleDrawingPreviewUris, uiState.previewUris)
    }
}