package com.qubacy.moveanddraw.domain.initial

import android.net.Uri
import app.cash.turbine.test
import com.qubacy.moveanddraw._common.util.mock.UriMockUtil
import com.qubacy.moveanddraw._common.util.rule.MainCoroutineRule
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.data.preview.repository.PreviewDataRepository
import com.qubacy.moveanddraw.data.preview.repository.source.local.LocalPreviewDataSource
import com.qubacy.moveanddraw.domain.initial.result.GetExamplePreviewsResult
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

class InitialUseCaseTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(Dispatchers.IO)

    private lateinit var mInitialUseCase: InitialUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initUseCase(
        previewUris: List<Uri>,
        coroutineScope: CoroutineScope = GlobalScope,
        coroutineDispatcher: CoroutineDispatcher = mainCoroutineRule.coroutineDispatcher
    ) {
        val localPreviewDataSourceMock = Mockito.mock(LocalPreviewDataSource::class.java)

        Mockito.`when`(localPreviewDataSourceMock.getExamplePreviews()).thenReturn(previewUris)

        val errorDataRepositoryMock = Mockito.mock(ErrorDataRepository::class.java)

        val previewDataRepository = Mockito.mock(PreviewDataRepository::class.java)

        Mockito.`when`(previewDataRepository.getExamplePreviews()).thenReturn(previewUris)

        mInitialUseCase = InitialUseCase(errorDataRepositoryMock, previewDataRepository).apply {
            setCoroutineScope(coroutineScope)
            setCoroutineDispatcher(coroutineDispatcher)
        }
    }

    @Before
    fun setup() {

    }

    @Test
    fun getExamplePreviewsTest(): Unit = runTest {
        val previewUris = listOf(UriMockUtil.getMockedUri())

        initUseCase(previewUris, this)

        mInitialUseCase.resultFlow.test {
            skipItems(1)
            mInitialUseCase.getExamplePreviews()

            val result = awaitItem()!!

            Assert.assertEquals(GetExamplePreviewsResult::class, result::class)
            Assert.assertEquals(previewUris, (result as GetExamplePreviewsResult).previewUris)
        }
    }
}