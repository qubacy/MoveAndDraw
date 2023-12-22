package com.qubacy.moveanddraw.data.preview.repository

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.qubacy.moveanddraw._common.util.livedata.getOrAwaitValue
import com.qubacy.moveanddraw._common.util.mock.UriMockUtil
import com.qubacy.moveanddraw._common.util.rule.MainCoroutineRule
import com.qubacy.moveanddraw.data.preview.repository.source.LocalPreviewDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class PreviewDataRepositoryTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(Dispatchers.IO)

    private lateinit var mPreviewDataRepository: PreviewDataRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initRepository(
        previewUris: List<Uri>,
        coroutineScope: CoroutineScope = GlobalScope,
        coroutineDispatcher: CoroutineDispatcher = mainCoroutineRule.coroutineDispatcher
    ) {
        val localPreviewDataSourceMock = Mockito.mock(LocalPreviewDataSource::class.java)

        Mockito.`when`(localPreviewDataSourceMock.getExamplePreviews()).thenReturn(previewUris)

        mPreviewDataRepository = PreviewDataRepository(localPreviewDataSourceMock).apply {
            setCoroutineScope(coroutineScope)
            setCoroutineDispatcher(coroutineDispatcher)
        }
    }

    @Before
    fun setup() {

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getExamplePreviewsTest(): Unit = mainCoroutineRule.run {
        val previewUris = listOf(UriMockUtil.getMockedUri())

        initRepository(previewUris, this)

        val resultLiveData = mPreviewDataRepository.getExamplePreviews()

        Assert.assertEquals(previewUris, resultLiveData.getOrAwaitValue())
    }
}