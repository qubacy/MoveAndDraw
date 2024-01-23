package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.util

import android.opengl.GLES20

object GL2Util {
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
}