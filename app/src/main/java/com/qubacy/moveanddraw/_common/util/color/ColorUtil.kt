package com.qubacy.moveanddraw._common.util.color

import android.graphics.Color
import androidx.annotation.FloatRange

object ColorUtil {
    fun toRGBA(
        @FloatRange(0.0, 1.0) r: Float,
        @FloatRange(0.0, 1.0) g: Float,
        @FloatRange(0.0, 1.0) b: Float,
        @FloatRange(0.0, 1.0) a: Float
    ): Int {
        return Color.argb(
            (a * 255).toInt(),
            (r * 255).toInt(),
            (g * 255).toInt(),
            (b * 255).toInt()
        )
    }
}