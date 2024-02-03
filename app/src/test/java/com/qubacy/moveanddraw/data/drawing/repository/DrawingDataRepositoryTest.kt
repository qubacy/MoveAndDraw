package com.qubacy.moveanddraw.data.drawing.repository

import android.net.Uri
import com.qubacy.moveanddraw._common._test.util.mock.AnyMockUtil
import com.qubacy.moveanddraw._common._test.util.mock.UriMockUtil
import com.qubacy.moveanddraw.data.drawing.model.DataDrawing
import com.qubacy.moveanddraw.data.drawing.repository.source.local.LocalDrawingDataSource
import com.qubacy.moveanddraw.data.drawing.repository.source.local.result.SaveNewFileResult
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
        savedExistingDrawingPath: String = String(),
        saveNewFileResult: SaveNewFileResult = SaveNewFileResult(String(), UriMockUtil.getMockedUri())
    ) {
        val localDrawingDataSourceMock = mock(LocalDrawingDataSource::class.java)

        Mockito.`when`(localDrawingDataSourceMock.load(AnyMockUtil.anyObject<Uri>()))
            .thenReturn(loadedDrawing)
        Mockito.`when`(localDrawingDataSourceMock.saveChanges(
            AnyMockUtil.anyObject<DataDrawing>(), AnyMockUtil.anyObject<Uri>())
        ).thenReturn(savedExistingDrawingPath)
        Mockito.`when`(localDrawingDataSourceMock.saveNewFile(
            AnyMockUtil.anyObject<DataDrawing>(), Mockito.anyString())
        ).thenReturn(saveNewFileResult)

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

        val saveNewFileResult = SaveNewFileResult(String(), UriMockUtil.getMockedUri())

        initRepository(saveNewFileResult = saveNewFileResult)

        val gottenSaveNewDrawingResult =
            mDrawingDataRepository.saveNewDrawing(savedNewDrawing, savedDrawingFilename)

        Assert.assertEquals(saveNewFileResult.filePath, gottenSaveNewDrawingResult.filePath)
    }

    @Test
    fun saveDrawingTest() {
        val savedDrawingUri = UriMockUtil.getMockedUri()
        val savedNewDrawing = DataDrawing(floatArrayOf(), floatArrayOf(), floatArrayOf(), arrayOf())

        val savedNewDrawingFilePath = String()

        initRepository(savedExistingDrawingPath = savedNewDrawingFilePath)

        val gottenSaveDrawingResult =
            mDrawingDataRepository.saveDrawing(savedNewDrawing, savedDrawingUri)

        Assert.assertEquals(savedNewDrawingFilePath, gottenSaveDrawingResult.filePath)
    }
}