package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.model._common

interface AccelerometerStateHolder {
    val xLastOffset: Float
    val yLastOffset: Float
    val zLastOffset: Float

    fun setLastOffsets(xOffset: Float, yOffset: Float, zOffset: Float)
}