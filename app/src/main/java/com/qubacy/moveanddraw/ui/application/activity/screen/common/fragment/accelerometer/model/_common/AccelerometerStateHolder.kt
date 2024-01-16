package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.model._common

interface AccelerometerStateHolder {
    fun applyAccelerations(xAcceleration: Float, yAcceleration: Float, zAcceleration: Float) {

    }

    fun applyAccelerations(offsets: FloatArray) {
        applyAccelerations(offsets[0], offsets[1], offsets[2])
    }

    fun getOffsetsByAccelerations(
        xAcceleration: Float, yAcceleration: Float, zAcceleration: Float
    ): FloatArray

    data class GetOffsetByAccelerationResult(
        val curVelocity: Float,
        val distance: Float
    )

    fun getOffsetByAcceleration(
        acceleration: Float,
        updateTime: Long,
        prevVelocity: Float
    ): GetOffsetByAccelerationResult
}