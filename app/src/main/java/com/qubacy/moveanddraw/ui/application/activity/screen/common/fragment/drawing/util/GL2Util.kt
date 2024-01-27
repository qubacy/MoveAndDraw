package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.util

import android.opengl.GLES20
import android.util.Log
import com.qubacy.moveanddraw.domain._common.model.drawing._common.DrawingContext
import kotlin.math.sqrt

object GL2Util {
    const val TAG = "GL2Util"

    fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    fun polygonToTriangles(polygonVertexIdArray: ShortArray): List<Short> {
        if (polygonVertexIdArray.size == 3) return polygonVertexIdArray.toList()

        val trianglesVertices = mutableListOf<Short>()

        for (i in 1 until polygonVertexIdArray.size - 1) {
            val curTriangleVertices = listOf(
                polygonVertexIdArray[0], polygonVertexIdArray[i], polygonVertexIdArray[i + 1]
            )

            trianglesVertices.addAll(curTriangleVertices)
        }

        return trianglesVertices
    }

    fun getMaxDistanceFromDot(vertices: FloatArray, centerDot: FloatArray): Float {
        var maxDistance = 0f

        for (i in vertices.indices step DrawingContext.COORDS_PER_VERTEX) {
            val distance = sqrt(
                (vertices[i] - centerDot[0]) * (vertices[i] - centerDot[0])
                        + (vertices[i + 1] - centerDot[1]) * (vertices[i + 1] - centerDot[1])
                        + (vertices[i + 2] - centerDot[2]) * (vertices[i + 2] - centerDot[2])
            )

            if (distance > maxDistance) maxDistance = distance

            Log.d(TAG, "getMaxDistanceFromDot(): vertex = (${vertices[i]},${vertices[i + 1]},${vertices[i + 2]});")
            Log.d(TAG, "getMaxDistanceFromDot(): distance = $distance;")
        }

        return maxDistance
    }

    fun getVertexCenterPoint(figureVertexArray: FloatArray): FloatArray {
        if (figureVertexArray.size < DrawingContext.COORDS_PER_VERTEX)
            return floatArrayOf(0f, 0f, 0f)

        var minX = figureVertexArray[0]
        var maxX = figureVertexArray[0]

        var minY = figureVertexArray[1]
        var maxY = figureVertexArray[1]

        var minZ = figureVertexArray[2]
        var maxZ = figureVertexArray[2]


        for (i in figureVertexArray.indices step DrawingContext.COORDS_PER_VERTEX) {
            if (figureVertexArray[i + 0] < minX) minX = figureVertexArray[i + 0]
            if (figureVertexArray[i + 0] > maxX) maxX = figureVertexArray[i + 0]

            if (figureVertexArray[i + 1] < minY) minY = figureVertexArray[i + 1]
            if (figureVertexArray[i + 1] > maxY) maxY = figureVertexArray[i + 1]

            if (figureVertexArray[i + 2] < minZ) minZ = figureVertexArray[i + 2]
            if (figureVertexArray[i + 2] > maxZ) maxZ = figureVertexArray[i + 2]
        }

        return floatArrayOf(
            (minX + maxX) / 2,
            (minY + maxY) / 2,
            (minZ + maxZ) / 2
        )
    }
}