package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.mapper

import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model.GLDrawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.util.GL2Util

class DrawingGLDrawingMapperImpl : DrawingGLDrawingMapper {
    private fun mapFace(face: Array<Triple<Int, Int?, Int?>>): List<Int> {
        val faceVertexIdArray = face.map { it.first }.toIntArray()

        return GL2Util.polygonToTriangles(faceVertexIdArray)
    }

    override fun map(drawing: Drawing): GLDrawing {
        val vertexDrawingOrder = mutableListOf<Int>()

        for (face in drawing.faceArray) {
            val curPolygon = mapFace(face)

            vertexDrawingOrder.addAll(curPolygon)
        }

        return GLDrawing(
            drawing.vertexArray.flatMap { it.toList() }.toFloatArray(),
            vertexDrawingOrder.toIntArray()
        )
    }
}