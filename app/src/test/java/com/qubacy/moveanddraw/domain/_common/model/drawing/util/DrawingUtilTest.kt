package com.qubacy.moveanddraw.domain._common.model.drawing.util

import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.domain._common.model.drawing._test.util.DrawingGeneratorUtil
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DrawingUtilTest {
    private data class FilterVertexArrayWithFacesTestCase(
        val drawing: Drawing,
        val narrowedFaces: Array<Array<Triple<Int, Int?, Int?>>>,
        val finalVertices: Array<Triple<Float, Float, Float>>,
        val finalFaces: Array<Array<Triple<Int, Int?, Int?>>>
    )

    @Before
    fun setup() {

    }

    @Test
    fun filterVertexArrayWithFacesTest() {
        val normalizedSquareDrawing = DrawingGeneratorUtil.generateDrawingByVerticesFaces(
            null,
            arrayOf(
                Triple(0f, 0f, 0f),
                Triple(0f, 1f, 0f),
                Triple(1f, 1f, 0f),
                Triple(1f, 0f, 0f)
            ),
            arrayOf(
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
        val chaoticSquareDrawing = DrawingGeneratorUtil.generateDrawingByVerticesFaces(
            null,
            arrayOf(
                Triple(0f, 0f, 0f),
                Triple(0f, 1f, 0f),
                Triple(1f, 1f, 0f),
                Triple(1f, 0f, 0f)
            ),
            arrayOf(
                arrayOf(
                    Triple(1, null, null),
                    Triple(2, null, null),
                    Triple(3, null, null)
                ),
                arrayOf(
                    Triple(1, null, null),
                    Triple(3, null, null),
                    Triple(0, null, null)
                )
            )
        )

        val testCases = arrayOf(
            FilterVertexArrayWithFacesTestCase(
                normalizedSquareDrawing,
                normalizedSquareDrawing.faceArray.sliceArray(0 until normalizedSquareDrawing.faceArray.size - 1),
                normalizedSquareDrawing.vertexArray.sliceArray(0 until normalizedSquareDrawing.vertexArray.size - 1),
                normalizedSquareDrawing.faceArray.sliceArray(0 until normalizedSquareDrawing.faceArray.size - 1)
            ),
            FilterVertexArrayWithFacesTestCase(
                chaoticSquareDrawing,
                chaoticSquareDrawing.faceArray.sliceArray(0 until normalizedSquareDrawing.faceArray.size - 1),
                chaoticSquareDrawing.vertexArray.sliceArray(1 until normalizedSquareDrawing.vertexArray.size),
                arrayOf(
                    arrayOf(
                        Triple(0, null, null),
                        Triple(1, null, null),
                        Triple(2, null, null)
                    )
                )
            ),
        )

        for (testCase in testCases) {
            val gottenVerticesFaces = DrawingUtil
                .filterVertexArrayWithFaces(testCase.drawing.vertexArray, testCase.narrowedFaces)

            Assert.assertArrayEquals(testCase.finalVertices, gottenVerticesFaces.first)
            Assert.assertArrayEquals(testCase.finalFaces, gottenVerticesFaces.second)
        }
    }
}