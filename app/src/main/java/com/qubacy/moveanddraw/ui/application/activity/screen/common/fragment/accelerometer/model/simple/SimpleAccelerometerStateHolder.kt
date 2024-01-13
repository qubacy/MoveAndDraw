package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.model.simple

import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.model._common.AccelerometerStateHolder

class SimpleAccelerometerStateHolder(

) : AccelerometerStateHolder {
    private var mXLastOffset: Float = 0f
    override val xLastOffset get() = mXLastOffset

    private var mYLastOffset: Float = 0f
    override val yLastOffset get() = mYLastOffset

    private var mZLastOffset: Float = 0f
    override val zLastOffset get() = mZLastOffset

    override fun setLastOffsets(xOffset: Float, yOffset: Float, zOffset: Float) {
        mXLastOffset = xOffset
        mYLastOffset = yOffset
        mZLastOffset = zOffset
    }
}