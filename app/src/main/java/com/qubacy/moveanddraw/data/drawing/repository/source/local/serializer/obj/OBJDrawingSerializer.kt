package com.qubacy.moveanddraw.data.drawing.repository.source.local.serializer.obj

import com.qubacy.moveanddraw.data.drawing.model.DataDrawing
import com.qubacy.moveanddraw.data.drawing.repository.source.local._common.obj.OBJContext
import com.qubacy.moveanddraw.data.drawing.repository.source.local.serializer._common.DrawingSerializer

class OBJDrawingSerializer : DrawingSerializer {
    override fun serialize(drawing: DataDrawing): String {
        val serializedVertices = serializeVertices(drawing.vertexArray)
        val serializedFaces = serializeFaces(drawing.faceArray)

        return (serializedVertices + serializedFaces)
    }

    private fun serializeVertices(vertices: FloatArray): String {
        val serializedVerticesBuilder = StringBuilder()

        for (i in vertices.indices step DataDrawing.VERTEX_COORD_COUNT) {
            serializedVerticesBuilder.append(OBJContext.VERTEX_TOKEN).append(" ")

            serializedVerticesBuilder.append(vertices[i]).append(" ")
            serializedVerticesBuilder.append(vertices[i + 1]).append(" ")
            serializedVerticesBuilder.append(vertices[i + 2]).append("\n")
        }

        return serializedVerticesBuilder.toString()
    }

    private fun serializeFaces(faceArray: Array<Array<Triple<Short, Short?, Short?>>>): String {
        val serializedFacesBuilder = StringBuilder()

        for (i in faceArray.indices) {
            val curFace = faceArray[i]

            serializedFacesBuilder.append(OBJContext.FACE_TOKEN).append(" ")

            for (j in curFace.indices) {
                val curVertex = curFace[j]

                serializedFacesBuilder.append(curVertex.first + 1)

                if (curVertex.second != null)
                    serializedFacesBuilder.append(OBJContext.PARTS_SPLITTER)
                        .append(curVertex.second!! + 1)
                if (curVertex.third != null) {
                    if (curVertex.second == null)
                        serializedFacesBuilder.append(OBJContext.PARTS_SPLITTER)

                    serializedFacesBuilder.append(OBJContext.PARTS_SPLITTER)
                        .append(curVertex.third!! + 1)
                }

                if (j != curFace.size - 1) serializedFacesBuilder.append(" ")
            }

            if (i != faceArray.size - 1) serializedFacesBuilder.append("\n")
        }

        return serializedFacesBuilder.toString()
    }
}