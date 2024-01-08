package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.mapper

import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model.GLDrawing
import java.lang.IllegalArgumentException

class DrawingGLDrawingMapperImpl : DrawingGLDrawingMapper {
    override fun map(drawing: Drawing): GLDrawing {
        val vertexDrawingOrder = mutableListOf<Short>()

        for (face in drawing.faceArray) {
            val curPolygon =
                if (face.size == 3) { face.map { it.first } }
                else if (face.size == 4) {
                    val vertices = face.map { it.first }

                    vertices.plus(arrayOf(vertices[2], vertices[0], vertices[3]))
                }
                else throw IllegalArgumentException()

            vertexDrawingOrder.addAll(curPolygon)
        }

        return GLDrawing(
            drawing.vertexArray,
            vertexDrawingOrder.toShortArray()
        )
    }
}