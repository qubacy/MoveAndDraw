package com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas._common

object EditorCanvasContext {
    enum class Mode(val id: Short) {
        VIEWING(0), CREATING_FACE(1);

        companion object {
            fun getModeById(id: Short): Mode {
                return values().find { it.id == id }!!
            }
        }
    }
}