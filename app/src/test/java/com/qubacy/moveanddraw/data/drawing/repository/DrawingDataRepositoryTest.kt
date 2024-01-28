package com.qubacy.moveanddraw.data.drawing.repository

import android.net.Uri
import com.qubacy.moveanddraw._common._test.util.mock.AnyMockUtil
import com.qubacy.moveanddraw._common._test.util.mock.UriMockUtil
import com.qubacy.moveanddraw.data.drawing.model.DataDrawing
import com.qubacy.moveanddraw.data.drawing.repository.source.local.LocalDrawingDataSource
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

class DrawingDataRepositoryTest {
    private lateinit var mDrawingDataRepository: DrawingDataRepository

    private fun initRepository(
        loadedDrawing: DataDrawing =
            DataDrawing(floatArrayOf(), floatArrayOf(), floatArrayOf(), arrayOf()),
        savedDrawingPath: String = String()
    ) {
        val localDrawingDataSourceMock = mock(LocalDrawingDataSource::class.java)

        Mockito.`when`(localDrawingDataSourceMock.load(AnyMockUtil.anyObject<Uri>()))
            .thenReturn(loadedDrawing)
        Mockito.`when`(localDrawingDataSourceMock.saveChanges(
            AnyMockUtil.anyObject<DataDrawing>(), AnyMockUtil.anyObject<Uri>())
        ).thenReturn(savedDrawingPath)
        Mockito.`when`(localDrawingDataSourceMock.saveNewFile(
            AnyMockUtil.anyObject<DataDrawing>(), Mockito.anyString())
        ).thenReturn(savedDrawingPath)

        mDrawingDataRepository = DrawingDataRepository(localDrawingDataSourceMock)
    }

    @Before
    fun setup() {
        initRepository()
    }

    @Test
    fun loadDrawingTest() {
        val drawingUri = UriMockUtil.getMockedUri()
        val loadedDrawing = DataDrawing(floatArrayOf(), floatArrayOf(), floatArrayOf(), arrayOf())

        initRepository(loadedDrawing = loadedDrawing)

        val gottenLoadedDrawing = mDrawingDataRepository.loadDrawing(drawingUri)

        Assert.assertEquals(loadedDrawing, gottenLoadedDrawing)
    }

    @Test
    fun saveNewDrawingTest() {
        val savedDrawingFilename = String()
        val savedNewDrawing = DataDrawing(floatArrayOf(), floatArrayOf(), floatArrayOf(), arrayOf())

        val savedNewDrawingFilePath = String()

        initRepository(savedDrawingPath = savedNewDrawingFilePath)

        val gottenSavedNewDrawingFilePath =
            mDrawingDataRepository.saveNewDrawing(savedNewDrawing, savedDrawingFilename)

        Assert.assertEquals(savedNewDrawingFilePath, gottenSavedNewDrawingFilePath)
    }

    @Test
    fun saveDrawingTest() {
        val savedDrawingUri = UriMockUtil.getMockedUri()
        val savedNewDrawing = DataDrawing(floatArrayOf(), floatArrayOf(), floatArrayOf(), arrayOf())

        val savedNewDrawingFilePath = String()

        initRepository(savedDrawingPath = savedNewDrawingFilePath)

        val gottenSavedNewDrawingFilePath =
            mDrawingDataRepository.saveDrawing(savedNewDrawing, savedDrawingUri)

        Assert.assertEquals(savedNewDrawingFilePath, gottenSavedNewDrawingFilePath)
    }
}