package com.qubacy.moveanddraw.domain._common.model.drawing.util

import com.qubacy.moveanddraw.domain._common.model.drawing._common.DrawingContext

fun FloatArray.toVertexTripleArray(): Array<Triple<Float, Float, Float>> {
    val vertexTripleList = mutableListOf<Triple<Float, Float, Float>>()

    for (i in indices step DrawingContext.COORDS_PER_VERTEX)
        vertexTripleList.add(Triple(this[i], this[i + 1], this[i + 2]))

    return vertexTripleList.toTypedArray()
}