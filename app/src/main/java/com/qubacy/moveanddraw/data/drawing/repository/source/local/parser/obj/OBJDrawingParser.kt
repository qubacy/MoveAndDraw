package com.qubacy.moveanddraw.data.drawing.repository.source.local.parser.obj

import com.qubacy.moveanddraw.data.drawing.model.DataDrawing
import com.qubacy.moveanddraw.data.drawing.repository.source.local.parser._common.DrawingParser
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

class OBJDrawingParser : DrawingParser {
    companion object {
        const val TOKEN_SPLITTER = " "
        const val PARTS_SPLITTER = "/"

        const val VERTEX_TOKEN = "v"
        const val NORMAL_TOKEN = "vn"
        const val TEXTURE_TOKEN = "vt"
        const val INDEX_TOKEN = "f"
    }

    override fun parseStream(inputStream: InputStream): DataDrawing {
        val reader = BufferedReader(InputStreamReader(inputStream))

        val vertices = ArrayList<Float>()
        val normals = ArrayList<Float>()
        val textures = ArrayList<Float>()
        val indices = ArrayList<Int>()

        readVectors(reader, vertices, normals, textures, indices)

        val vertexArray = FloatArray(indices.size * 3)
        val normalArray = FloatArray(indices.size * 3)
        val textureArray = FloatArray(indices.size * 2)

        processVectors(vertices, normals, textures, indices, vertexArray, normalArray, textureArray)

        val vertexBuffer = ByteBuffer.allocateDirect(vertexArray.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexArray)
        val normalBuffer = ByteBuffer.allocateDirect(normalArray.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(normalArray)
        val textureBuffer = ByteBuffer.allocateDirect(textureArray.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(textureArray)

        vertexBuffer.position(0)
        normalBuffer.position(0)
        textureBuffer.position(0)

        return DataDrawing(vertexBuffer, normalBuffer, textureBuffer, indices.size);
    }

    private fun readVectors(
        reader: BufferedReader,
        vertices: ArrayList<Float>,
        normals: ArrayList<Float>,
        textures: ArrayList<Float>,
        indices: ArrayList<Int>
    ) {
        var line: String? = null

        while (true) {
            line = reader.readLine()

            if (line == null) break

            val tokens = line.split(TOKEN_SPLITTER);

            when (tokens[0]) {
                VERTEX_TOKEN -> {
                    vertices.add(tokens[1].toFloat())
                    vertices.add(tokens[2].toFloat())
                    vertices.add(tokens[3].toFloat())

                    break
                }
                NORMAL_TOKEN -> {
                    normals.add(tokens[1].toFloat())
                    normals.add(tokens[2].toFloat())
                    normals.add(tokens[3].toFloat())

                    break
                }
                TEXTURE_TOKEN -> {
                    textures.add(tokens[1].toFloat())
                    textures.add(tokens[2].toFloat())

                    break
                }
                INDEX_TOKEN -> {
                    for (i in 1 until tokens.size) {
                        val parts = tokens[i].split(PARTS_SPLITTER)

                        indices.add(parts[0].toInt() - 1);
                        indices.add(parts[1].toInt() - 1);
                        indices.add(parts[2].toInt() - 1);
                    }

                    break;
                }
            }
        }
    }

    private fun processVectors(
        vertices: ArrayList<Float>,
        normals: ArrayList<Float>,
        textures: ArrayList<Float>,
        indices: ArrayList<Int>,
        vertexArray: FloatArray,
        normalArray: FloatArray,
        textureArray: FloatArray
    ) {
        for (i in 0 until indices.size) {
            val vertexIndex = indices[i] * 3
            val normalIndex = indices[i] * 3 + 2
            val textureIndex = indices[i] * 2 + 1

            vertexArray[i * 3] = vertices[vertexIndex]
            vertexArray[i * 3 + 1] = vertices[vertexIndex + 1]
            vertexArray[i * 3 + 2] = vertices[vertexIndex + 2]

            normalArray[i * 3] = normals[normalIndex]
            normalArray[i * 3 + 1] = normals[normalIndex - 1]
            normalArray[i * 3 + 2] = normals[normalIndex - 2]

            textureArray[i * 2] = textures[textureIndex]
            textureArray[i * 2 + 1] = textures[textureIndex - 1]
        }
    }
}