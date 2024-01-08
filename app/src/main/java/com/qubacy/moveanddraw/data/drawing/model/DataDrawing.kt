package com.qubacy.moveanddraw.data.drawing.model

import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing

data class DataDrawing(
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

fun DataDrawing.toDrawing(): Drawing {
    //return Drawing(vertexBuffer, normalBuffer, textureBuffer, numVertices)
    return Drawing(vertexArray, normalArray, textureArray, faceArray)//numVertices)
}