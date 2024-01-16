package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.model.simple

import android.util.Log
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.model._common.AccelerometerStateHolder

class SimpleAccelerometerStateHolder(

) : AccelerometerStateHolder {
    companion object {
        const val TAG = "SIMPLE_ACCEL_SH"
    }

    private var mPrevUpdateTime: Long = 0L

    private var mXPrevVelocity: Float = 0f
    private var mYPrevVelocity: Float = 0f
    private var mZPrevVelocity: Float = 0f

    override fun getOffsetsByAccelerations(
        xAcceleration: Float, yAcceleration: Float, zAcceleration: Float
    ): FloatArray {
        val updateTime = System.currentTimeMillis()

        if (mPrevUpdateTime == 0L) {
            mPrevUpdateTime = updateTime

            return floatArrayOf(0f, 0f, 0f)
        }

        val xResult = getOffsetByAcceleration(xAcceleration, updateTime, mXPrevVelocity)
        val yResult = getOffsetByAcceleration(yAcceleration, updateTime, mYPrevVelocity)
        val zResult = getOffsetByAcceleration(zAcceleration, updateTime, mZPrevVelocity)

        mXPrevVelocity = xResult.curVelocity
        mYPrevVelocity = yResult.curVelocity
        mZPrevVelocity = zResult.curVelocity

        mPrevUpdateTime = updateTime

        Log.d(TAG, "getOffsetsByAccelerations(): " +
            "xOffset = ${xResult.distance}; " +
            "yOffset = ${yResult.distance}; " +
            "zOffset = ${zResult.distance};"
        )

        return floatArrayOf(
            xResult.distance,
            yResult.distance,
            zResult.distance
        )
    }

    override fun getOffsetByAcceleration(
        acceleration: Float,
        updateTime: Long,
        prevVelocity: Float
    ): AccelerometerStateHolder.GetOffsetByAccelerationResult {
        val interval = (updateTime - mPrevUpdateTime) / 1000f

        val curVelocity: Float = prevVelocity + acceleration * interval
        val distance = prevVelocity * interval + 0.5f * acceleration * interval * interval

        return AccelerometerStateHolder.GetOffsetByAccelerationResult(curVelocity, distance)
    }
}