package com.qubacy.moveanddraw._common._test.util.value

import kotlin.math.abs

object ValueUtil {
    fun floorValue(value: Float, floor: Float = 0.001f): Float {
        return if (abs(value) < floor) 0f else value
    }

    fun floorValue(value: Double, floor: Double = 0.001): Double {
        return if (abs(value) < floor) 0.0 else value
    }
}