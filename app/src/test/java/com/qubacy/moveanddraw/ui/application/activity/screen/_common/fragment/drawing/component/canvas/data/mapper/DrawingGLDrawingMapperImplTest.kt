package com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing.component.canvas.data.mapper

import com.qubacy.moveanddraw._common._test.util.mock.UriMockUtil
import com.qubacy.moveanddraw.domain._common.model.drawing._test.util.DrawingGeneratorUtil
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.mapper.DrawingGLDrawingMapperImpl
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model.GLDrawing
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DrawingGLDrawingMapperImplTest {
    private lateinit var mMapper: DrawingGLDrawingMapperImpl

    @Before
    fun setup() {
        mMapper = DrawingGLDrawingMapperImpl()
    }

    @Test
    fun mapTest() {
        val mockedUri = UriMockUtil.getMockedUri()

        val squareDrawing = DrawingGeneratorUtil.generateSquareDrawing()
        val cubeDrawing = DrawingGeneratorUtil.generateCubeDrawing()

        val cubeSquarePolygonDrawing = DrawingGeneratorUtil.generateDrawingByVerticesFaces(
            mockedUri,
            arrayOf(
                Triple(0f, 0f, 0f),
                Triple(0f, 1f, 0f),
                Triple(1f, 1f, 0f),
                Triple(1f, 0f, 0f),
                Triple(0f, 0f, 1f),
                Triple(0f, 1f, 0f),
                Triple(1f, 1f, 1f),
                Triple(1f, 0f, 1f)
            ),
            arrayOf(
                arrayOf(
                    Triple(0, null, null),
                    Triple(1, null, null),
                    Triple(2, null, null),
                    Triple(3, null, null),
                ),
                arrayOf(
                    Triple(4, null, null),
                    Triple(5, null, null),
                    Triple(6, null, null),
                    Triple(7, null, null),
                ),
                arrayOf(
                    Triple(0, null, null),
                    Triple(4, null, null),
                    Triple(5, null, null),
                    Triple(1, null, null),
                ),
                arrayOf(
                    Triple(1, null, null),
                    Triple(5, null, null),
                    Triple(6, null, null),
                    Triple(2, null, null),
                ),
                arrayOf(
                    Triple(2, null, null),
                    Triple(6, null, null),
                    Triple(7, null, null),
                    Triple(3, null, null),
                ),
                arrayOf(
                    Triple(0, null, null),
                    Triple(4, null, null),
                    Triple(7, null, null),
                    Triple(3, null, null),
                ),
            )
        )

        val drawingGLDrawingPairs = arrayOf(
            Pair(
                squareDrawing,
                GLDrawing(
                    squareDrawing.vertexArray.flatMap { listOf(it.first, it.second, it.third) }
                        .toFloatArray(),
                    squareDrawing.faceArray.flatMap { it.map { vertex -> vertex.first } }
                        .toIntArray()
                )
            ),
            Pair(
                cubeDrawing,
                GLDrawing(
                    cubeDrawing.vertexArray.flatMap { listOf(it.first, it.second, it.third) }
                        .toFloatArray(),
                    cubeDrawing.faceArray.flatMap { it.map { vertex -> vertex.first } }
                        .toIntArray()
                )
            ),
            Pair(
                cubeSquarePolygonDrawing,
                GLDrawing(
                    cubeSquarePolygonDrawing.vertexArray.flatMap { listOf(it.first, it.second, it.third) }
                        .toFloatArray(),
                    intArrayOf(
                        cubeSquarePolygonDrawing.faceArray[0][0].first, cubeSquarePolygonDrawing.faceArray[0][1].first, cubeSquarePolygonDrawing.faceArray[0][2].first,
                        cubeSquarePolygonDrawing.faceArray[0][0].first, cubeSquarePolygonDrawing.faceArray[0][2].first, cubeSquarePolygonDrawing.faceArray[0][3].first,
                        cubeSquarePolygonDrawing.faceArray[1][0].first, cubeSquarePolygonDrawing.faceArray[1][1].first, cubeSquarePolygonDrawing.faceArray[1][2].first,
                        cubeSquarePolygonDrawing.faceArray[1][0].first, cubeSquarePolygonDrawing.faceArray[1][2].first, cubeSquarePolygonDrawing.faceArray[1][3].first,
                        cubeSquarePolygonDrawing.faceArray[2][0].first, cubeSquarePolygonDrawing.faceArray[2][1].first, cubeSquarePolygonDrawing.faceArray[2][2].first,
                        cubeSquarePolygonDrawing.faceArray[2][0].first, cubeSquarePolygonDrawing.faceArray[2][2].first, cubeSquarePolygonDrawing.faceArray[2][3].first,
                        cubeSquarePolygonDrawing.faceArray[3][0].first, cubeSquarePolygonDrawing.faceArray[3][1].first, cubeSquarePolygonDrawing.faceArray[3][2].first,
                        cubeSquarePolygonDrawing.faceArray[3][0].first, cubeSquarePolygonDrawing.faceArray[3][2].first, cubeSquarePolygonDrawing.faceArray[3][3].first,
                        cubeSquarePolygonDrawing.faceArray[4][0].first, cubeSquarePolygonDrawing.faceArray[4][1].first, cubeSquarePolygonDrawing.faceArray[4][2].first,
                        cubeSquarePolygonDrawing.faceArray[4][0].first, cubeSquarePolygonDrawing.faceArray[4][2].first, cubeSquarePolygonDrawing.faceArray[4][3].first,
                        cubeSquarePolygonDrawing.faceArray[5][0].first, cubeSquarePolygonDrawing.faceArray[5][1].first, cubeSquarePolygonDrawing.faceArray[5][2].first,
                        cubeSquarePolygonDrawing.faceArray[5][0].first, cubeSquarePolygonDrawing.faceArray[5][2].first, cubeSquarePolygonDrawing.faceArray[5][3].first,
                    )
                )
            ),
        )

        for (drawingExpectedGLDrawingPair in drawingGLDrawingPairs) {
            val gottenGLDrawing = mMapper.map(drawingExpectedGLDrawingPair.first)

            Assert.assertEquals(drawingExpectedGLDrawingPair.second, gottenGLDrawing)
        }
    }
}