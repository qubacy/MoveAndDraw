package com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing.util

import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.util.GL2Util
import org.junit.Assert
import org.junit.Test
import kotlin.math.abs

class GL2UtilTest {
    private class PolygonToTrianglesTestCase(
        val polygonVertexIdArray: IntArray,
        val resultTrianglesVertices: IntArray
    )

    @Test
    fun polygonToTrianglesTest() {
        val testCases = arrayOf(
            PolygonToTrianglesTestCase(
                intArrayOf(0, 1, 2),
                intArrayOf(0, 1, 2)
            ),
            PolygonToTrianglesTestCase(
                intArrayOf(0, 1, 2, 3),
                intArrayOf(0, 1, 2, 0, 2, 3)
            ),
            PolygonToTrianglesTestCase(
                intArrayOf(0, 1, 2, 3, 4),
                intArrayOf(0, 1, 2, 0, 2, 3, 0, 3, 4)
            ),
            PolygonToTrianglesTestCase(
                intArrayOf(0, 1, 2, 3, 4, 5, 6),
                intArrayOf(0, 1, 2, 0, 2, 3, 0, 3, 4, 0, 4, 5, 0, 5, 6)
            ),
            PolygonToTrianglesTestCase(
                intArrayOf(0, 5, 10, 2),
                intArrayOf(0, 5, 10, 0, 10, 2)
            ),
        )

        for (testCase in testCases) {
            val gottenTrianglesVertices = GL2Util.polygonToTriangles(testCase.polygonVertexIdArray)

            Assert.assertArrayEquals(
                testCase.resultTrianglesVertices, gottenTrianglesVertices.toIntArray())
        }
    }

    private class GetMaxDistanceFromDotTestCase(
        val vertices: FloatArray,
        val centerDot: FloatArray,
        val maxDistance: Float
    )

    @Test
    fun getMaxDistanceFromDotTest() {
        val testCases = arrayOf(
            GetMaxDistanceFromDotTestCase(
                floatArrayOf(
                    0f, 0f, 0f,
                    0f, 1f, 0f,
                    1f, 1f, 0f,
                    1f, 0f, 0f
                ),
                floatArrayOf(0f, 0f, 0f),
                1.414f
            ),
            GetMaxDistanceFromDotTestCase(
                floatArrayOf(
                    0f, 0f, 0f,
                    0f, 100f, 100f,
                ),
                floatArrayOf(0f, 0f, 0f),
                141.421f
            ),
            GetMaxDistanceFromDotTestCase(
                floatArrayOf(
                    -0.5f, -0.5f, -0.5f,
                    -0.5f, 0.5f, -0.5f,
                    0.5f, 0.5f, -0.5f,
                    0.5f, -0.5f, -0.5f,
                    -0.5f, -0.5f, 0.5f,
                    -0.5f, 0.5f, 0.5f,
                    0.5f, 0.5f, 0.5f,
                    0.5f, -0.5f, 0.5f
                ),
                floatArrayOf(0f, 0f, 0f),
                0.866f
            )
        )

        for (testCase in testCases) {
            val gottenMaxDistance = GL2Util.getMaxDistanceFromDot(
                testCase.vertices, testCase.centerDot)
            val difference = abs(gottenMaxDistance - testCase.maxDistance)

            Assert.assertTrue(difference < 0.001f)
        }
    }

    private class GetVertexCenterPointTestCase(
        val figureVertexArray: FloatArray,
        val resultCenterPoint: FloatArray
    )

    @Test
    fun getVertexCenterPointTest() {
        val testCases = arrayOf(
            GetVertexCenterPointTestCase(
                floatArrayOf(
                    0f, 0f, 0f,
                    1f, 1f, 0f,
                ),
                floatArrayOf(0.5f, 0.5f, 0f)
            ),
            GetVertexCenterPointTestCase(
                floatArrayOf(
                    0f, 0f, 0f,
                    0f, 1f, 0f,
                    1f, 1f, 0f,
                    1f, 0f, 0f
                ),
                floatArrayOf(0.5f, 0.5f, 0f)
            ),
            GetVertexCenterPointTestCase(
                floatArrayOf(
                    0f, 0f, 0f,
                    0f, 1f, 0f,
                    1f, 1f, 0f,
                    1f, 0f, 0f,
                    0f, 0f, 1f,
                    0f, 1f, 1f,
                    1f, 1f, 1f,
                    1f, 0f, 1f
                ),
                floatArrayOf(0.5f, 0.5f, 0.5f)
            ),
            GetVertexCenterPointTestCase(
                floatArrayOf(
                    -0.5f, -0.5f, -0.5f,
                    -0.5f, 0.5f, -0.5f,
                    0.5f, 0.5f, -0.5f,
                    0.5f, -0.5f, -0.5f,
                    -0.5f, -0.5f, 0.5f,
                    -0.5f, 0.5f, 0.5f,
                    0.5f, 0.5f, 0.5f,
                    0.5f, -0.5f, 0.5f
                ),
                floatArrayOf(0f, 0f, 0f)
            ),
        )

        for (testCase in testCases) {
            val gottenCenterPoint = GL2Util.getVertexCenterPoint(testCase.figureVertexArray)

            Assert.assertTrue(testCase.resultCenterPoint.contentEquals(gottenCenterPoint))
        }
    }
}