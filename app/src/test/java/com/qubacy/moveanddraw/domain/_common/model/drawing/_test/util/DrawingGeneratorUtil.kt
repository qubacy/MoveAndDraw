package com.qubacy.moveanddraw.domain._common.model.drawing._test.util

import android.net.Uri
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing

object DrawingGeneratorUtil {
    fun generateDrawingByVerticesFaces(
        mockedUri: Uri? = null,
        vertices: Array<Triple<Float, Float, Float>>,
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

    fun generateSquareDrawing(uri: Uri? = null): Drawing {
        return generateDrawingByVerticesFaces(
            uri,
            arrayOf(
                Triple(0f, 0f, 0f),
                Triple(0f, 1f, 0f),
                Triple(1f, 1f, 0f),
                Triple(1f, 0f, 0f)
            ),
            arrayOf(
                arrayOf(
                    Triple(0, null, null),
                    Triple(1, null, null),
                    Triple(2, null, null)
                ),
                arrayOf(
                    Triple(0, null, null),
                    Triple(2, null, null),
                    Triple(3, null, null)
                )
            )
        )
    }

    fun generateCubeDrawing(uri: Uri? = null): Drawing {
        return generateDrawingByVerticesFaces(
            uri,
            arrayOf(
                Triple(0f, 0f, 0f),
                Triple(0f, 1f, 0f),
                Triple(1f, 1f, 0f),
                Triple(1f, 0f, 0f),
                Triple(0f, 0f, 1f),
                Triple(0f, 1f, 0f),
                Triple(1f, 1f, 1f),
                Triple(1f, 0f, 1f)
            ),
            arrayOf(
                arrayOf(
                    Triple(0, null, null),
                    Triple(1, null, null),
                    Triple(2, null, null)
                ),
                arrayOf(
                    Triple(0, null, null),
                    Triple(2, null, null),
                    Triple(3, null, null)
                ),
                arrayOf(
                    Triple(4, null, null),
                    Triple(5, null, null),
                    Triple(6, null, null)
                ),
                arrayOf(
                    Triple(4, null, null),
                    Triple(6, null, null),
                    Triple(7, null, null)
                ),
                arrayOf(
                    Triple(0, null, null),
                    Triple(1, null, null),
                    Triple(5, null, null)
                ),
                arrayOf(
                    Triple(0, null, null),
                    Triple(5, null, null),
                    Triple(4, null, null)
                ),
                arrayOf(
                    Triple(1, null, null),
                    Triple(2, null, null),
                    Triple(6, null, null)
                ),
                arrayOf(
                    Triple(1, null, null),
                    Triple(6, null, null),
                    Triple(5, null, null)
                ),
                arrayOf(
                    Triple(2, null, null),
                    Triple(3, null, null),
                    Triple(7, null, null)
                ),
                arrayOf(
                    Triple(2, null, null),
                    Triple(7, null, null),
                    Triple(6, null, null)
                ),
                arrayOf(
                    Triple(0, null, null),
                    Triple(1, null, null),
                    Triple(7, null, null)
                ),
                arrayOf(
                    Triple(0, null, null),
                    Triple(7, null, null),
                    Triple(4, null, null)
                )
            )
        )
    }
}