package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common

typealias Dot2D = Pair<Float, Float>

object GLContext {
    const val COORDS_PER_DOT = 2

    enum class DrawingMode(val id: Int) {
        FILLED(0), SKETCH(1), OUTLINED(2);

        companion object {
            fun getDrawingModeById(id: Int): DrawingMode {
                return DrawingMode.values().first { it.id == id }
            }
        }
    }

    fun floatArrayToDot2DList(dotArray: FloatArray): List<Dot2D> {
        val resultFaceSketchDotBuffer = mutableListOf<Dot2D>()

        for (i in dotArray.indices step COORDS_PER_DOT)
            resultFaceSketchDotBuffer.add(Dot2D(dotArray[i], dotArray[i + 1]))

        return resultFaceSketchDotBuffer
    }
}