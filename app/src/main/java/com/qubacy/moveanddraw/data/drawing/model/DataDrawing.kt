package com.qubacy.moveanddraw.data.drawing.model

import android.net.Uri
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing

data class DataDrawing(
    val vertexArray: FloatArray,
    val normalArray: FloatArray,
    val textureArray: FloatArray,
    val faceArray: Array<Array<Triple<Short, Short?, Short?>>>
) {
    companion object {
        const val VERTEX_COORD_COUNT = 3
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataDrawing

        if (!vertexArray.contentEquals(other.vertexArray)) return false
        if (!normalArray.contentEquals(other.normalArray)) return false
        if (!textureArray.contentEquals(other.textureArray)) return false
        if (!faceArray.contentDeepEquals(other.faceArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vertexArray.contentHashCode()
        result = 31 * result + normalArray.contentHashCode()
        result = 31 * result + textureArray.contentHashCode()
        result = 31 * result + faceArray.contentDeepHashCode()
        return result
    }

}

fun DataDrawing.toDrawing(uri: Uri): Drawing {
    val vertices = mutableListOf<Triple<Float, Float, Float>>()

    for (i in vertexArray.indices step DataDrawing.VERTEX_COORD_COUNT)
        vertices.add(Triple(vertexArray[i], vertexArray[i + 1], vertexArray[i + 2]))

    return Drawing(uri, vertices.toTypedArray(), normalArray, textureArray, faceArray)
}