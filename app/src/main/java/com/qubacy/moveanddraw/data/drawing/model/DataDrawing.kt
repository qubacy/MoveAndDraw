package com.qubacy.moveanddraw.data.drawing.model

import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing
import java.nio.FloatBuffer

data class DataDrawing(
    val vertexBuffer: FloatBuffer,
    val normalBuffer: FloatBuffer,
    val textureBuffer: FloatBuffer,
    val numVertices: Int
) {

}

fun DataDrawing.toDrawing(): Drawing {
    return Drawing(vertexBuffer, normalBuffer, textureBuffer, numVertices)
}