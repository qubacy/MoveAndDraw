package com.qubacy.moveanddraw.ui.application.activity.screen.initial.model

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.qubacy.moveanddraw._common.util.mock.UriMockUtil
import com.qubacy.moveanddraw.data.preview.repository.PreviewDataRepository
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class InitialViewModelTest {
    private lateinit var mInitialViewModel: InitialViewModel

    private fun initViewModel(
        previewUris: List<Uri> = listOf()
    ) {
        val previewDataRepositoryMock = Mockito.mock(PreviewDataRepository::class.java)

        Mockito.`when`(previewDataRepositoryMock.getExamplePreviews())
            .thenReturn(MutableLiveData(previewUris))

        mInitialViewModel = InitialViewModel(previewDataRepositoryMock)
    }

    @Before
    fun setup() {

    }

    @Test
    fun getExampleDrawingPreviewsTest() {
        val exampleDrawingPreviewUris = listOf(UriMockUtil.getMockedUri())

        initViewModel(exampleDrawingPreviewUris)

        val gottenPreviewUrisLiveData = mInitialViewModel.getExampleDrawingPreviews()

        Assert.assertEquals(exampleDrawingPreviewUris, gottenPreviewUrisLiveData.value)
    }
}