package com.qubacy.moveanddraw.data.drawing.repository.source.local.parser.obj

import com.qubacy.moveanddraw.data.drawing.model.DataDrawing
import com.qubacy.moveanddraw.data.drawing.repository.source.local.parser._common.DrawingParser
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class OBJDrawingParser : DrawingParser {
    companion object {
        const val TOKEN_SPLITTER = " "
        const val PARTS_SPLITTER = "/"

        const val VERTEX_TOKEN = "v"
        const val NORMAL_TOKEN = "vn"
        const val TEXTURE_TOKEN = "vt"
        const val FACE_TOKEN = "f"
    }

    override fun parseStream(inputStream: InputStream): DataDrawing {
        val reader = BufferedReader(InputStreamReader(inputStream))

        val finalVertices = mutableListOf<Float>()
        val finalNormals = mutableListOf<Float>()
        val finalTextures = mutableListOf<Float>()
        val finalFaces = mutableListOf<Array<Triple<Short, Short?, Short?>>>()

        readVectors(reader, finalVertices, finalNormals, finalTextures, finalFaces)

        return DataDrawing(
            finalVertices.toFloatArray(),
            finalNormals.toFloatArray(),
            finalTextures.toFloatArray(),
            finalFaces.toTypedArray()
        )
    }

    private fun readVectors(
        reader: BufferedReader,
        finalVertices: MutableList<Float>,
        finalNormals: MutableList<Float>,
        finalTextures: MutableList<Float>,
        facesList: MutableList<Array<Triple<Short, Short?, Short?>>>
    ) {
        var line: String? = null

        while (true) {
            line = reader.readLine()

            if (line == null) break

            val tokens = line.split(TOKEN_SPLITTER)

            when (tokens[0]) {
                VERTEX_TOKEN -> {
                    finalVertices.add(tokens[1].toFloat())
                    finalVertices.add(tokens[2].toFloat())
                    finalVertices.add(tokens[3].toFloat())
                }
                NORMAL_TOKEN -> {
                    finalNormals.add(tokens[1].toFloat())
                    finalNormals.add(tokens[2].toFloat())
                    finalNormals.add(tokens[3].toFloat())
                }
                TEXTURE_TOKEN -> {
                    finalTextures.add(tokens[1].toFloat())
                    finalTextures.add(tokens[2].toFloat())
                }
                FACE_TOKEN -> {
                    val curFaceList = mutableListOf<Triple<Short, Short?, Short?>>()

                    for (i in 1 until tokens.size) {
                        val vun = tokens[i].split(PARTS_SPLITTER)

                        val vertexIndex = vun[0].toShort().minus(1).toShort()
                        val textureIndex =
                            if (vun.size > 1) vun[1].toShortOrNull()?.minus(1)?.toShort()
                            else null
                        val normalIndex =
                            if (vun.size > 2) vun[2].toShortOrNull()?.minus(1)?.toShort()
                            else null

                        val point = Triple(vertexIndex, textureIndex, normalIndex)

                        curFaceList.add(point)
                    }

                    facesList.add(curFaceList.toTypedArray())
                }
            }
        }
    }
}