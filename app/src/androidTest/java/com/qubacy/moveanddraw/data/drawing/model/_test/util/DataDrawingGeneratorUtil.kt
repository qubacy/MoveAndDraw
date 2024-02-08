package com.qubacy.moveanddraw.data.drawing.model._test.util

import com.qubacy.moveanddraw.data.drawing.model.DataDrawing

object DataDrawingGeneratorUtil {
    fun generateDataDrawingWithVerticesAndFaces(
        vertexArray: FloatArray,
        faceArray: Array<Array<Triple<Int, Int?, Int?>>>
    ): DataDrawing {
        return DataDrawing(
            vertexArray,
            floatArrayOf(),
            floatArrayOf(),
            faceArray
        )
    }

    fun generateSquareDataDrawing(): DataDrawing {
        return generateDataDrawingWithVerticesAndFaces(
            floatArrayOf(
                0f, 0f, 0f,
                0f, 1f, 0f,
                1f, 1f, 0f,
                1f, 0f, 0f
            ),
            arrayOf(
                arrayOf(Triple(0, null, null), Triple(1, null, null), Triple(2, null, null)),
                arrayOf(Triple(0, null, null), Triple(2, null, null), Triple(3, null, null)),
            )
        )
    }
}