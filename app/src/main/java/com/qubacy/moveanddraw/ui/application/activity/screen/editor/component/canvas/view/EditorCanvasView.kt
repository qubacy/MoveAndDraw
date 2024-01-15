package com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.view

import android.content.Context
import android.util.AttributeSet
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.view.CanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.renderer.EditorCanvasRenderer

class EditorCanvasView(
    context: Context, attributeSet: AttributeSet
) : CanvasView(
    context, attributeSet
) {
    override val mRenderer: EditorCanvasRenderer = EditorCanvasRenderer()

    private var mDeviceX: Float = 0f
    private var mDeviceY: Float = 0f
    private var mDeviceZ: Float = 0f

    suspend fun changeDevicePosition(xOffset: Float, yOffset: Float, zOffset: Float) {
        mDeviceX += xOffset
        mDeviceY += yOffset
        mDeviceZ += zOffset

        mRenderer.setDeviceDrawingPosition(mDeviceX, mDeviceY, mDeviceZ)
    }
}