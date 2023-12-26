package com.qubacy.moveanddraw.data.preview.repository

import android.net.Uri
import com.qubacy.moveanddraw._common.util.mock.UriMockUtil
import com.qubacy.moveanddraw.data.preview.repository.source.local.LocalPreviewDataSource
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class PreviewDataRepositoryTest {
    private lateinit var mPreviewDataRepository: PreviewDataRepository

    private fun initRepository(
        previewUris: List<Uri>
    ) {
        val localPreviewDataSourceMock = Mockito.mock(LocalPreviewDataSource::class.java)

        Mockito.`when`(localPreviewDataSourceMock.getExamplePreviews()).thenReturn(previewUris)

        mPreviewDataRepository = PreviewDataRepository(localPreviewDataSourceMock)
    }

    @Before
    fun setup() {

    }

    @Test
    fun getExamplePreviewsTest() {
        val previewUris = listOf(UriMockUtil.getMockedUri())

        initRepository(previewUris)

        val gottenPreviewUris = mPreviewDataRepository.getExamplePreviews()

        Assert.assertEquals(previewUris, gottenPreviewUris)
    }
}