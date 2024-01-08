package com.qubacy.moveanddraw.domain._common.model.drawing

data class Drawing(
//    val vertexBuffer: FloatBuffer,
//    val normalBuffer: FloatBuffer,
//    val textureBuffer: FloatBuffer,
    val vertexArray: FloatArray,
    val normalArray: FloatArray,
    val textureArray: FloatArray,
    val faceArray: Array<Array<Triple<Short, Short?, Short?>>>
    //val numVertices: Int
) {

}