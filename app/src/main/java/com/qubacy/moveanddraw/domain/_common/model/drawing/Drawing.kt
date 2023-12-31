package com.qubacy.moveanddraw.domain._common.model.drawing

import java.nio.FloatBuffer

data class Drawing(
    val vertexBuffer: FloatBuffer,
    val normalBuffer: FloatBuffer,
    val textureBuffer: FloatBuffer,
    val numVertices: Int
) {

}