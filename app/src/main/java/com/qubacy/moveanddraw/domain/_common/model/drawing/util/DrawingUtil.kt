package com.qubacy.moveanddraw.domain._common.model.drawing.util

import com.qubacy.moveanddraw.domain._common.model.drawing._common.DrawingContext

fun FloatArray.toVertexTripleArray(): Array<Triple<Float, Float, Float>> {
    val vertexTripleList = mutableListOf<Triple<Float, Float, Float>>()

    for (i in indices step DrawingContext.COORDS_PER_VERTEX)
        vertexTripleList.add(Triple(this[i], this[i + 1], this[i + 2]))

    return vertexTripleList.toTypedArray()
}

fun Array<Triple<Float, Float, Float>>.toFloatArray(): FloatArray {
    return flatMap { it.toList() }.toFloatArray()
}

object DrawingUtil {
    /**
     * Returns a filtered VERTEX TRIPLE ARRAY & FACES;
     */
    fun filterVertexArrayWithFaces(
        vertexTripleArray: Array<Triple<Float, Float, Float>>,
        faces: Array<Array<Triple<Short, Short?, Short?>>>
    ): Pair<Array<Triple<Float, Float, Float>>, Array<Array<Triple<Short, Short?, Short?>>>> {
        val usedVertices = faces.flatMap { it.map { index -> index.first } }.toSet()

        val resultVertexTripleList = mutableListOf<Triple<Float, Float, Float>>()
        val vertexOffsetList = mutableListOf<Short>()

        for (i in vertexTripleArray.indices) {
            if (usedVertices.contains(i.toShort())) {
                resultVertexTripleList.add(vertexTripleArray[i])

                continue
            }

            vertexOffsetList.add(i.toShort())
        }

        val updatedFaces = mutableListOf<Array<Triple<Short, Short?, Short?>>>()

        for (face in faces) {
            val updatedFace = mutableListOf<Triple<Short, Short?, Short?>>()

            for (vertex in face) {
                var offset = 0

                for (i in vertexOffsetList.indices) {
                    if (vertex.first < vertexOffsetList[i]) break

                    ++offset
                }

                updatedFace.add(
                    Triple((vertex.first - offset).toShort(), vertex.second, vertex.third)
                )
            }

            updatedFaces.add(updatedFace.toTypedArray())
        }

        return Pair(resultVertexTripleList.toTypedArray(), updatedFaces.toTypedArray())
    }
}