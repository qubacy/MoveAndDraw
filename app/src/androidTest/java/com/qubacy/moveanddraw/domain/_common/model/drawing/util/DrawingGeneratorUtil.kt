package com.qubacy.moveanddraw.domain._common.model.drawing.util

import android.net.Uri
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing

object DrawingGeneratorUtil {
    fun generateSquareDrawing(): Drawing {
        return Drawing(
            Uri.parse(String()),
            arrayOf(
                Triple(0f, 0f, 0f),
                Triple(0f, 1f, 0f),
                Triple(1f, 0f, 0f),
                Triple(1f, 1f, 0f)
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