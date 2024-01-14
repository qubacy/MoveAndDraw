package com.qubacy.moveanddraw.data.drawing.repository.source.local.serializer.obj

import com.qubacy.moveanddraw.data.drawing.model.DataDrawing
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class OBJDrawingSerializerTest {
    private lateinit var mOBJDrawingSerializer: OBJDrawingSerializer

    @Before
    fun setup() {
        mOBJDrawingSerializer = OBJDrawingSerializer()
    }

    @Test
    fun serializeTest() {
        val drawingStringArray = arrayOf(
            Pair(
                DataDrawing(
                    floatArrayOf(
                        0f, 0f, 0f,
                        0f, 1f, 0f,
                        1f, 1f, 0f,
                        1f, 0f, 0f
                    ),
                    floatArrayOf(),
                    floatArrayOf(),
                    arrayOf(
                        arrayOf(Triple(0, null, null), Triple(1, null, null), Triple(2, null, null)),
                        arrayOf(Triple(0, null, null), Triple(2, null, null), Triple(3, null, null))
                    )
                ),
                "v 0.0 0.0 0.0\n" +
                "v 0.0 1.0 0.0\n" +
                "v 1.0 1.0 0.0\n" +
                "v 1.0 0.0 0.0\n" +
                "f 1 2 3\n" +
                "f 1 3 4"
            ),
            Pair(
                DataDrawing(
                    floatArrayOf(
                        0f, 0f, 0f,
                        0f, 1f, 0f,
                        1f, 1f, 0f,
                        1f, 0f, 0f,
                        0f, 0f, 1f,
                        0f, 1f, 1f,
                        1f, 1f, 1f,
                        1f, 0f, 1f
                    ),
                    floatArrayOf(),
                    floatArrayOf(),
                    arrayOf(
                        arrayOf(Triple(0, null, null), Triple(1, null, null), Triple(2, null, null)),
                        arrayOf(Triple(0, null, null), Triple(2, null, null), Triple(3, null, null)),
                        arrayOf(Triple(4, null, null), Triple(5, null, null), Triple(6, null, null)),
                        arrayOf(Triple(4, null, null), Triple(6, null, null), Triple(7, null, null)),
                        arrayOf(Triple(0, null, null), Triple(1, null, null), Triple(5, null, null)),
                        arrayOf(Triple(0, null, null), Triple(5, null, null), Triple(4, null, null)),
                        arrayOf(Triple(1, null, null), Triple(2, null, null), Triple(6, null, null)),
                        arrayOf(Triple(1, null, null), Triple(6, null, null), Triple(5, null, null)),
                        arrayOf(Triple(2, null, null), Triple(3, null, null), Triple(7, null, null)),
                        arrayOf(Triple(2, null, null), Triple(7, null, null), Triple(6, null, null)),
                        arrayOf(Triple(0, null, null), Triple(3, null, null), Triple(7, null, null)),
                        arrayOf(Triple(0, null, null), Triple(7, null, null), Triple(4, null, null))
                    )
                ),
                "v 0.0 0.0 0.0\n" +
                "v 0.0 1.0 0.0\n" +
                "v 1.0 1.0 0.0\n" +
                "v 1.0 0.0 0.0\n" +
                "v 0.0 0.0 1.0\n" +
                "v 0.0 1.0 1.0\n" +
                "v 1.0 1.0 1.0\n" +
                "v 1.0 0.0 1.0\n" +
                "f 1 2 3\n" +
                "f 1 3 4\n" +
                "f 5 6 7\n" +
                "f 5 7 8\n" +
                "f 1 2 6\n" +
                "f 1 6 5\n" +
                "f 2 3 7\n" +
                "f 2 7 6\n" +
                "f 3 4 8\n" +
                "f 3 8 7\n" +
                "f 1 4 8\n" +
                "f 1 8 5"
            )
        )

        for (drawingString in drawingStringArray) {
            val gottenSerializedDrawing = mOBJDrawingSerializer.serialize(drawingString.first)

            Assert.assertEquals(drawingString.second, gottenSerializedDrawing)
        }
    }
}