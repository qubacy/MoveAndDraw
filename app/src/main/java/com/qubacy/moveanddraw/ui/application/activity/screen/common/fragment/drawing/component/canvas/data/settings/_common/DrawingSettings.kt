package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.settings._common

import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.GLContext
import java.io.Serializable

open class DrawingSettings(
    initDrawingMode: GLContext.DrawingMode,
    initModelColor: FloatArray
) : Serializable {
    @Volatile
    protected var mDrawingMode: GLContext.DrawingMode = initDrawingMode
    @Volatile
    protected var mModelColor: FloatArray = initModelColor

    val drawingMode get() = mDrawingMode
    val modelColor get() = mModelColor

    fun copy(): DrawingSettings {
        return DrawingSettings(
            mDrawingMode,
            mModelColor.clone()
        )
    }
}