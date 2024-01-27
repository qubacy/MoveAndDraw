package com.qubacy.moveanddraw.data.drawing.repository.source.local

import android.net.Uri
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qubacy.moveanddraw.data.drawing.model.DataDrawing
import com.qubacy.moveanddraw.data.drawing.model._test.util.DataDrawingGeneratorUtil
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class LocalDrawingDataSourceTest {
    private lateinit var mLocalDrawingDataSource: LocalDrawingDataSource

    @Before
    fun setup() {
        mLocalDrawingDataSource = LocalDrawingDataSource(
            InstrumentationRegistry.getInstrumentation().targetContext)
    }

    private fun fileAbsolutePathToUri(fileAbsolutePath: String): Uri {
        return File(fileAbsolutePath).toUri()
    }

    /**
     * Returns ABSOLUTE PATH & Uri
     */
    private fun saveNewFile(drawing: DataDrawing, filename: String): Pair<String, Uri> {
        val savedFileAbsPath = mLocalDrawingDataSource.context.filesDir.absolutePath + "/$filename"
        val gottenSavedFileAbsPath = mLocalDrawingDataSource.saveNewFile(drawing, filename)

        Assert.assertEquals(savedFileAbsPath, gottenSavedFileAbsPath)

        return Pair(gottenSavedFileAbsPath, fileAbsolutePathToUri(gottenSavedFileAbsPath))
    }

    @Test
    fun saveNewFileThenLoadTest() {
        val drawingToSave = DataDrawingGeneratorUtil.generateSquareDataDrawing()
        val filename = "test.obj"

        val savedFileUri = saveNewFile(drawingToSave, filename).second

        val gottenDrawing = mLocalDrawingDataSource.load(savedFileUri)

        Assert.assertEquals(drawingToSave, gottenDrawing)
    }

    @Test
    fun saveNewFileThenSaveChangesAndLoadTest() {
        val drawingToSave = DataDrawingGeneratorUtil.generateSquareDataDrawing()
        val filename = "test.obj"

        val savedFileUri = saveNewFile(drawingToSave, filename)

        val modifiedDrawing = DataDrawingGeneratorUtil.generateDataDrawingWithVerticesAndFaces(
            drawingToSave.vertexArray,
            drawingToSave.faceArray.sliceArray(0 until drawingToSave.faceArray.size - 1)
        )

        val savedWithChangesAbsPath =
            mLocalDrawingDataSource.saveChanges(modifiedDrawing, savedFileUri.second)

        Assert.assertEquals(savedFileUri.first, savedWithChangesAbsPath)

        val savedWithChangesFileUri = fileAbsolutePathToUri(savedWithChangesAbsPath)

        val gottenDrawing = mLocalDrawingDataSource.load(savedWithChangesFileUri)

        Assert.assertEquals(modifiedDrawing, gottenDrawing)
    }
}