package com.qubacy.moveanddraw.domain._common.model.drawing

data class Drawing(
    val vertexArray: FloatArray,
    val normalArray: FloatArray,
    val textureArray: FloatArray,
    val faceArray: Array<Array<Triple<Short, Short?, Short?>>>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Drawing

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