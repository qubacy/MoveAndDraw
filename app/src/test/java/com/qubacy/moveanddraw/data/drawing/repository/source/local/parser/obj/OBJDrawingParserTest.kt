package com.qubacy.moveanddraw.data.drawing.repository.source.local.parser.obj

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class OBJDrawingParserTest {
    private lateinit var mOBJDrawingParser: OBJDrawingParser

    private fun initParser() {
        mOBJDrawingParser = OBJDrawingParser()
    }

    @Before
    fun setup() {
        initParser()
    }

    @Test
    fun parseStreamTest() {
        val vertices = arrayOf(
            arrayOf(0f, 0f, 0f),
            arrayOf(1f, 0f, 0f),
            arrayOf(0f, 1f, 0f),
            arrayOf(1f, 1f, 0f)
        )
        val faces = arrayOf(
            arrayOf(1, 2, 3),
            arrayOf(3, 1, 4)
        )

        val drawingObjString = serializeObjComponents(vertices, faces)
        val drawingByteArray = drawingObjString.toByteArray()
        val inputStream = ByteArrayInputStream(drawingByteArray)

        val dataDrawing = mOBJDrawingParser.parseStream(inputStream)

        Assert.assertEquals(vertices.size * vertices.first().size, dataDrawing.vertexArray.size)
        Assert.assertEquals(2, dataDrawing.faceArray.size)

        for (i in 0 until dataDrawing.vertexArray.size step 3) {
            val curVertex = arrayOf(
                dataDrawing.vertexArray[i],
                dataDrawing.vertexArray[i + 1],
                dataDrawing.vertexArray[i + 2]
            )

            Assert.assertNotNull(vertices.find { it.contentEquals(curVertex) })
        }
        for (curFace in dataDrawing.faceArray) {
            val curFaceWithOnlyVertices = curFace.map { it.first + 1 }.toTypedArray()

            Assert.assertNotNull(faces.find { it.contentEquals(curFaceWithOnlyVertices) })
        }
    }

    private fun serializeObjComponents(
        vertices: Array<Array<Float>>,
        faces: Array<Array<Int>>
    ): String {
        return serializeVertices(vertices) + serializeFaces(faces)
    }

    private fun serializeVertices(vertices: Array<Array<Float>>): String {
        val resultStringBuilder = StringBuilder()

        for (vertex in vertices) {
            resultStringBuilder.append("v ${vertex[0]} ${vertex[1]} ${vertex[2]}\n")
        }

        return resultStringBuilder.toString()
    }

    private fun serializeFaces(faces: Array<Array<Int>>): String {
        val resultStringBuilder = StringBuilder()

        for (face in faces) {
            resultStringBuilder.append("f ${face[0]} ${face[1]} ${face[2]}\n")
        }

        return resultStringBuilder.toString()
    }
}