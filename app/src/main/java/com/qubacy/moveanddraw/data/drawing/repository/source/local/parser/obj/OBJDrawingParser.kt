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
        //val finalIndices = mutableListOf<Short>()
        val finalFaces = mutableListOf<Array<Triple<Short, Short?, Short?>>>()

        readVectors(reader, finalVertices, finalNormals, finalTextures, finalFaces) //finalIndices)

        //val vertexBuffer = floatListToFloatBuffer(finalVertices)
        //val normalBuffer = floatListToFloatBuffer(finalNormals)
        //val textureBuffer = floatListToFloatBuffer(finalTextures)

        //return DataDrawing(vertexBuffer, normalBuffer, textureBuffer, finalIndices.size);
        return DataDrawing(
            finalVertices.toFloatArray(),
            finalNormals.toFloatArray(),
            finalTextures.toFloatArray(),
            finalFaces.toTypedArray()
            //finalIndices.size
        )
    }

//    private fun floatListToFloatBuffer(list: List<Float>): FloatBuffer {
//        val buffer = ByteBuffer.allocateDirect(list.size * Float.SIZE_BYTES).apply {
//            order(ByteOrder.nativeOrder())
//        }
//
//        val resultBuffer = buffer.asFloatBuffer()
//
//        resultBuffer.put(list.toFloatArray())
//        buffer.position(0)
//        resultBuffer.position(0)
//
//        return resultBuffer
//    }

    private fun readVectors(
        reader: BufferedReader,
        finalVertices: MutableList<Float>,
        finalNormals: MutableList<Float>,
        finalTextures: MutableList<Float>,
        //finalIndices: MutableList<Short>
        facesList: MutableList<Array<Triple<Short, Short?, Short?>>>
    ) {
        val vertices = ArrayList<Triple<Float, Float, Float>>()
        val normals = ArrayList<Triple<Float, Float, Float>>()
        val textures = ArrayList<Pair<Float, Float>>()

        //val faceMap = HashMap<Triple<Int, Int?, Int?>, Short>()
        var nextIndex: Short = 0

        var line: String? = null

        while (true) {
            line = reader.readLine()

            if (line == null) break

            val tokens = line.split(TOKEN_SPLITTER)

            when (tokens[0]) {
                VERTEX_TOKEN -> {
//                    val vertex = Triple(
//                        tokens[1].toFloat(), tokens[2].toFloat(), tokens[3].toFloat())

//                    vertices.add(vertex)

                    finalVertices.add(tokens[1].toFloat())
                    finalVertices.add(tokens[2].toFloat())
                    finalVertices.add(tokens[3].toFloat())
                }
                NORMAL_TOKEN -> {
//                    val normal = Triple(
//                        tokens[1].toFloat(), tokens[2].toFloat(), tokens[3].toFloat())
//
//                    normals.add(normal)

                    finalNormals.add(tokens[1].toFloat())
                    finalNormals.add(tokens[2].toFloat())
                    finalNormals.add(tokens[3].toFloat())
                }
                TEXTURE_TOKEN -> {
//                    val texture = Pair(tokens[1].toFloat(), tokens[2].toFloat())
//
//                    textures.add(texture)

                    finalTextures.add(tokens[1].toFloat())
                    finalTextures.add(tokens[2].toFloat())
                }
                FACE_TOKEN -> {
                    val curFaceList = mutableListOf<Triple<Short, Short?, Short?>>()

                    for (i in 1 until tokens.size) {
                        val vun = tokens[i].split(PARTS_SPLITTER)
                        val point = Triple(
                            vun[0].toShort().minus(1).toShort(),
                            vun[1].toShortOrNull()?.minus(1)?.toShort(),
                            vun[2].toShortOrNull()?.minus(1)?.toShort()
                        )

                        curFaceList.add(point)


//                        val pointInFaceMap = faceMap[point]
//
//                        if (pointInFaceMap != null) {
//                            finalIndices.add(pointInFaceMap)
//
//                        } else {
//                            faceMap[point] = nextIndex
//
//                            finalVertices.add(vertices[point.first - 1].first)
//                            finalVertices.add(vertices[point.first - 1].second)
//                            finalVertices.add(vertices[point.first - 1].third)
//
//                            if (point.second != null) {
//                                finalTextures.add(textures[point.second!! - 1].first)
//                                finalTextures.add(textures[point.second!! - 1].second)
//                            }
//                            if (point.third != null) {
//                                finalNormals.add(normals[point.third!! - 1].first)
//                                finalNormals.add(normals[point.third!! - 1].second)
//                                finalNormals.add(normals[point.third!! - 1].third)
//                            }
//
//                            finalIndices.add(nextIndex++)
//                        }
                    }

                    facesList.add(curFaceList.toTypedArray())
                }
            }
        }
    }
}