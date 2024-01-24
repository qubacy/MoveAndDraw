package com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.data

data class FaceSketch(
    val vertexArray: Array<Triple<Float, Float, Float>>,
    val face: Array<Triple<Short, Short?, Short?>>
) {

}