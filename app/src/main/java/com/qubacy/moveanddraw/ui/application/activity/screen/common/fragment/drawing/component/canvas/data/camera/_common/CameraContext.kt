package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.camera._common

object CameraContext {
    const val MIN_FOV = Float.MIN_VALUE
    const val MAX_FOV = 180f

    const val DEFAULT_CAMERA_FOV = 60f

    const val DEFAULT_SCALE = 1f

    const val DEFAULT_MADE_WAY = 0f

    const val DEFAULT_CAMERA_NEAR = 0.01f
    const val CAMERA_NEAR_DRAWING_GAP = 0.001f

    fun checkScaleFactorValidity(scaleFactor: Float): Boolean {
        return (scaleFactor in (DEFAULT_CAMERA_FOV / MAX_FOV)  ..(DEFAULT_CAMERA_FOV / MIN_FOV))
    }
}