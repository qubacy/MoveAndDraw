package com.qubacy.moveanddraw._common.util.struct.array

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

fun IntArray.toNativeBuffer(): IntBuffer {
    return ByteBuffer.allocateDirect(this.size * Int.SIZE_BYTES).run {
        order(ByteOrder.nativeOrder())
        asIntBuffer().apply {
            put(this@toNativeBuffer)
            position(0)
        }
    }
}