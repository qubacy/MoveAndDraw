package com.qubacy.moveanddraw.domain.editor

import android.net.Uri
import app.cash.turbine.test
import com.qubacy.moveanddraw._common._test.data.InitData
import com.qubacy.moveanddraw._common._test.util.mock.AnyMockUtil
import com.qubacy.moveanddraw._common._test.util.mock.UriMockUtil
import com.qubacy.moveanddraw.data.drawing.model.DataDrawing
import com.qubacy.moveanddraw.data.drawing.repository.DrawingDataRepository
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.model.drawing._test.util.DrawingGeneratorUtil
import com.qubacy.moveanddraw.domain._common.usecase.drawing.DrawingUseCaseTest
import com.qubacy.moveanddraw.domain.editor._test.data.EditorUseCaseInitData
import com.qubacy.moveanddraw.domain.editor.result.face.add.AddNewFaceToDrawingResult
import com.qubacy.moveanddraw.domain.editor.result.face.remove.RemoveLastFaceFromDrawingResult
import com.qubacy.moveanddraw.domain.editor.result.save.SaveDrawingResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

class EditorUseCaseTest(

) : DrawingUseCaseTest<EditorUseCase>() {
    override fun generateDrawingUseCase(
        errorDataRepositoryMock: ErrorDataRepository,
        drawingDataRepositoryMock: DrawingDataRepository,
        initData: InitData?
    ): EditorUseCase {
        if (initData != null) {
            initData as EditorUseCaseInitData

            Mockito.`when`(drawingDataRepositoryMock.saveDrawing(
                AnyMockUtil.anyObject<DataDrawing>(), AnyMockUtil.anyObject<Uri>())
            ).thenReturn(initData.saveDrawingResult)
            Mockito.`when`(drawingDataRepositoryMock.saveNewDrawing(
                AnyMockUtil.anyObject<DataDrawing>(), Mockito.anyString())
            ).thenReturn(initData.saveDrawingResult)
        }

        return EditorUseCase(errorDataRepositoryMock, drawingDataRepositoryMock)
    }

    @Test
    fun saveDrawingWithExistingFileTest() = runTest {
        val savedDrawing = DrawingGeneratorUtil
            .generateDrawingByVerticesFaces(vertices = arrayOf(), faces = arrayOf())

        val savedDrawingFilePath = String()
        val mockedUri = UriMockUtil.getMockedUri()

        val saveDrawingResult = com.qubacy.moveanddraw.data.drawing.repository.result.save._common
            .SaveDrawingResult(savedDrawingFilePath, mockedUri)
        val initData = EditorUseCaseInitData(saveDrawingResult)

        initUseCase(initData = initData)

        mDrawingUseCase.resultFlow.test {
            skipItems(1)
            mDrawingUseCase.saveDrawing(savedDrawing ,mockedUri)

            val result = awaitItem()!!

            Assert.assertEquals(SaveDrawingResult::class, result::class)
            Assert.assertEquals(saveDrawingResult.filePath, (result as SaveDrawingResult).filePath)
        }
    }

    @Test
    fun saveNewDrawingTest() = runTest {
        val drawingFilename = String()

        val savedDrawing = DrawingGeneratorUtil
            .generateDrawingByVerticesFaces(vertices = arrayOf(), faces = arrayOf())

        val savedDrawingFilePath = String()
        val mockedUri = UriMockUtil.getMockedUri()

        val saveDrawingResult = com.qubacy.moveanddraw.data.drawing.repository.result.save._common
            .SaveDrawingResult(savedDrawingFilePath, mockedUri)
        val initData = EditorUseCaseInitData(saveDrawingResult)

        initUseCase(initData = initData)

        mDrawingUseCase.resultFlow.test {
            skipItems(1)
            mDrawingUseCase.saveDrawing(savedDrawing, filename = drawingFilename)

            val result = awaitItem()!!

            Assert.assertEquals(SaveDrawingResult::class, result::class)
            Assert.assertEquals(savedDrawingFilePath, (result as SaveDrawingResult).filePath)
        }
    }

    @Test
    fun removeLastFaceFromDrawingTest() = runTest {
        val drawing = DrawingGeneratorUtil
            .generateDrawingByVerticesFaces(
                vertices = arrayOf(
                    Triple(0f, 0f, 0f),
                    Triple(0f, 1f, 0f),
                    Triple(1f, 1f, 0f),
                    Triple(1f, 0f, 0f)
                ),
                faces = arrayOf(
                    arrayOf(
                        Triple(0, null, null),
                        Triple(1, null, null),
                        Triple(2, null, null)
                    ),
                    arrayOf(
                        Triple(0, null, null),
                        Triple(2, null, null),
                        Triple(3, null, null)
                    )
                )
            )
        val modifiedDrawing = DrawingGeneratorUtil.generateDrawingByVerticesFaces(
            vertices = drawing.vertexArray.sliceArray(0 until drawing.vertexArray.size - 1),
            faces = arrayOf(drawing.faceArray.first())
        )

        initUseCase()

        mDrawingUseCase.resultFlow.test {
            skipItems(1)
            mDrawingUseCase.removeLastFaceFromDrawing(drawing)

            val result = awaitItem()!!

            Assert.assertEquals(RemoveLastFaceFromDrawingResult::class, result::class)
            Assert.assertEquals(modifiedDrawing, (result as RemoveLastFaceFromDrawingResult).drawing)
        }
    }

    @Test
    fun addNewFaceToDrawingTest() = runTest {
        val drawing = DrawingGeneratorUtil
            .generateDrawingByVerticesFaces(
                vertices = arrayOf(
                    Triple(0f, 0f, 0f),
                    Triple(0f, 1f, 0f),
                    Triple(1f, 1f, 0f),
                    Triple(1f, 0f, 0f)
                ),
                faces = arrayOf(
                    arrayOf(
                        Triple(0, null, null),
                        Triple(1, null, null),
                        Triple(2, null, null)
                    ),
                    arrayOf(
                        Triple(0, null, null),
                        Triple(2, null, null),
                        Triple(3, null, null)
                    )
                )
            )

        val newFaceVertexTripleArray = arrayOf(
            Triple(0f, 0f, 1f),
            Triple(0f, 1f, 1f),
            Triple(1f, 1f, 1f),
            Triple(1f, 0f, 1f)
        )
        val newFace = arrayOf<Triple<Int, Int?, Int?>>(
            Triple(0, null, null),
            Triple(1, null, null),
            Triple(2, null, null),
            Triple(3, null, null)
        )

        val modifiedDrawing = DrawingGeneratorUtil.generateDrawingByVerticesFaces(
            vertices = drawing.vertexArray.plus(newFaceVertexTripleArray),
            faces = drawing.faceArray.plus(
                newFace.map {
                    Triple((it.first + drawing.vertexArray.size), it.second, it.third)
                }.toTypedArray()
            )
        )

        initUseCase()

        mDrawingUseCase.resultFlow.test {
            skipItems(1)
            mDrawingUseCase.addNewFaceToDrawing(drawing, newFaceVertexTripleArray, newFace)

            val result = awaitItem()!!

            Assert.assertEquals(AddNewFaceToDrawingResult::class, result::class)
            Assert.assertEquals(modifiedDrawing, (result as AddNewFaceToDrawingResult).drawing)
        }
    }
}