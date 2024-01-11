package com.qubacy.moveanddraw.domain._common.model.drawing.util

import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing

object DrawingGeneratorUtil {
    fun generateSquareDrawing(): Drawing {
        return Drawing(
            floatArrayOf(
                0f, 0f, 0f,
                0f, 1f, 0f,
                1f, 0f, 0f,
                1f, 1f, 0f
            ),
            floatArrayOf(),
            floatArrayOf(),
            arrayOf(
                arrayOf(
                    Triple(0, null, null),
                    Triple(1, null, null),
                    Triple(2, null, null)
                ),
                arrayOf(
                    Triple(2, null, null),
                    Triple(0, null, null),
                    Triple(4, null, null)
                ),
            )
        )
    }
}