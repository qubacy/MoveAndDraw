package com.qubacy.moveanddraw.domain._common.model.drawing._common

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.qubacy.moveanddraw.data.drawing.model.DataDrawing
import com.qubacy.moveanddraw.domain._common.model.drawing.util.toFloatArray
import com.qubacy.moveanddraw.domain._common.model.drawing.util.toVertexTripleArray

fun Parcel.writeFaceArray(faceArray: Array<Array<Triple<Short, Short?, Short?>>>) {
    writeInt(faceArray.size)

    for (face in faceArray) {
        writeInt(face.size)

        for (faceVertex in face) {
            val flatNormalizedVertex = faceVertex.toList().map {
                it ?: DrawingContext.NULL_FACE_VERTEX_PART_VALUE.toShort()
            }

            writeInt(faceVertex.first.toInt())
            writeInt(faceVertex.second?.toInt() ?: DrawingContext.NULL_FACE_VERTEX_PART_VALUE)
            writeInt(faceVertex.third?.toInt() ?: DrawingContext.NULL_FACE_VERTEX_PART_VALUE)
        }
    }
}

fun Parcel.readFaceArray(): Array<Array<Triple<Short, Short?, Short?>>> {
    val faceList: MutableList<Array<Triple<Short, Short?, Short?>>> = mutableListOf()

    val faceListSize = readInt()

    for (i in 0 until faceListSize) {
        val faceVertexListSize = readInt()
        val faceVertexList = mutableListOf<Triple<Short, Short?, Short?>>()

        for (j in 0 until faceVertexListSize) {
            val faceVertexArray = IntArray(3)

            readIntArray(faceVertexArray)

            val normalizedFaceVertexArray = faceVertexArray.map {
                if (it == DrawingContext.NULL_FACE_VERTEX_PART_VALUE) null else it.toShort()
            }
            val faceVertex = Triple<Short, Short?, Short?>(
                normalizedFaceVertexArray[0]!!,
                normalizedFaceVertexArray[1],
                normalizedFaceVertexArray[2]
            )

            faceVertexList.add(faceVertex)
        }

        faceList.add(faceVertexList.toTypedArray())
    }

    return faceList.toTypedArray()
}

data class Drawing(
    val uri: Uri? = null,
    val vertexArray: Array<Triple<Float, Float, Float>>,
    val normalArray: FloatArray,
    val textureArray: FloatArray,
    val faceArray: Array<Array<Triple<Short, Short?, Short?>>>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.createFloatArray()?.toVertexTripleArray()!!,
        parcel.createFloatArray() ?: floatArrayOf(),
        parcel.createFloatArray() ?: floatArrayOf(),
        parcel.readFaceArray()
    ) {

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Drawing

        if (uri != other.uri) return false
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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uri, flags)
        parcel.writeFloatArray(vertexArray.toFloatArray())
        parcel.writeFloatArray(normalArray)
        parcel.writeFloatArray(textureArray)
        parcel.writeFaceArray(faceArray)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Drawing> {
        override fun createFromParcel(parcel: Parcel): Drawing {
            return Drawing(parcel)
        }

        override fun newArray(size: Int): Array<Drawing?> {
            return arrayOfNulls(size)
        }
    }
}

fun Drawing.toDataDrawing(): DataDrawing {
    return DataDrawing(
        vertexArray.flatMap { it.toList() }.toFloatArray(),
        normalArray,
        textureArray,
        faceArray
    )
}