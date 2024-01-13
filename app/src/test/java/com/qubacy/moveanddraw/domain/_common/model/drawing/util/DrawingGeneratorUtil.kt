package com.qubacy.moveanddraw.domain._common.model.drawing.util

import android.net.Uri
import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing

object DrawingGeneratorUtil {
    fun generateDrawingByVerticesFaces(
        mockedUri: Uri,
        vertices: FloatArray,
        faces: Array<Array<Triple<Short, Short?, Short?>>>
    ): Drawing {
        return Drawing(
            mockedUri,
            vertices,
            floatArrayOf(),
            floatArrayOf(),
            faces
        )
    }
}