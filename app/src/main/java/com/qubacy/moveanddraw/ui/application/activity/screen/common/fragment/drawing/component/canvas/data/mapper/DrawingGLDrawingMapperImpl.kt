package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.mapper

import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model.GLDrawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.util.GL2Util

class DrawingGLDrawingMapperImpl : DrawingGLDrawingMapper {
    private fun mapFace(face: Array<Triple<Short, Short?, Short?>>): List<Short> {
        val faceVertexIdArray = face.map { it.first }.toShortArray()

        return GL2Util.polygonToTriangles(faceVertexIdArray)
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