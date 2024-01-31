package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.settings.mutable

import androidx.annotation.FloatRange
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.GLContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.settings._common.DrawingSettings

class MutableDrawingSettings(
    initDrawingMode: GLContext.DrawingMode,
    initModelColor: FloatArray
) : DrawingSettings(initDrawingMode, initModelColor) {
    fun setDrawingMode(drawingMode: GLContext.DrawingMode) {
        mDrawingMode = drawingMode
    }

    fun setModelColor(
        @FloatRange(0.0, 1.0) r: Float,
        @FloatRange(0.0, 1.0) g: Float,
        @FloatRange(0.0, 1.0) b: Float,
        @FloatRange(0.0, 1.0) a: Float
    ) {
        mModelColor = floatArrayOf(r, g, b, a)
    }

    fun setData(drawingSettings: DrawingSettings) {
        mDrawingMode = drawingSettings.drawingMode
        mModelColor = drawingSettings.modelColor
    }
}