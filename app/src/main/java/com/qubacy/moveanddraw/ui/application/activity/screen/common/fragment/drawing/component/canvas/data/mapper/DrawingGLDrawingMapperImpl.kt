package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.mapper

import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model.GLDrawing

class DrawingGLDrawingMapperImpl : DrawingGLDrawingMapper {
    private fun mapFace(face: Array<Triple<Short, Short?, Short?>>): List<Short> {
        if (face.size == 3) return face.map { it.first }

        val trianglesVertices = mutableListOf<Short>()

        for (i in 1 until face.size - 1) {
            val curTriangleVertices = listOf(
                face[0].first, face[i].first, face[i + 1].first
            )

            trianglesVertices.addAll(curTriangleVertices)
        }

        return trianglesVertices
    }

    override fun map(drawing: Drawing): GLDrawing {
        val vertexDrawingOrder = mutableListOf<Short>()

        for (face in drawing.faceArray) {
            val curPolygon = mapFace(face)

            vertexDrawingOrder.addAll(curPolygon)
        }

        return GLDrawing(
            drawing.vertexArray.flatMap { it.toList() }.toFloatArray(),
            vertexDrawingOrder.toShortArray()
        )
    }
}