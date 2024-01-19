package com.qubacy.moveanddraw._common.util.struct.array

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

fun ShortArray.toNativeBuffer(): ShortBuffer {
    return ByteBuffer.allocateDirect(this.size * Short.SIZE_BYTES).run {
        order(ByteOrder.nativeOrder())
        asShortBuffer().apply {
            put(this@toNativeBuffer)
            position(0)
        }
    }
}