package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common

typealias Dot2D = Pair<Float, Float>

object GLContext {
    enum class DrawingMode(val id: Int) {
        FILLED(0), SKETCH(1), OUTLINED(2);

        companion object {
            fun getDrawingModeById(id: Int): DrawingMode {
                return DrawingMode.values().first { it.id == id }
            }
        }
    }
}