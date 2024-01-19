package com.qubacy.moveanddraw._common.util.struct.array

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

fun FloatArray.toNativeBuffer(): FloatBuffer {
    return ByteBuffer.allocateDirect(size * Float.SIZE_BYTES).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(this@toNativeBuffer)
            position(0)
        }
    }
}