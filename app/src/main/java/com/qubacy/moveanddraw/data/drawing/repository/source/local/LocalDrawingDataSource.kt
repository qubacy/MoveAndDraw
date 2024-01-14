package com.qubacy.moveanddraw.data.drawing.repository.source.local

import android.content.Context
import android.net.Uri
import com.qubacy.moveanddraw.data._common.repository._common.source._common.DataSource
import com.qubacy.moveanddraw.data.drawing.model.DataDrawing
import com.qubacy.moveanddraw.data.drawing.repository.source.local.parser.obj.OBJDrawingParser
import com.qubacy.moveanddraw.data.drawing.repository.source.local.serializer.obj.OBJDrawingSerializer
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import javax.inject.Inject

class LocalDrawingDataSource @Inject constructor(
    val context: Context
) : DataSource {
    private val mDrawingParser = OBJDrawingParser()
    private val mDrawingSerializer = OBJDrawingSerializer()

    fun load(drawingUri: Uri): DataDrawing {
        var stream: InputStream? = null

        try {
            stream = context.contentResolver.openInputStream(drawingUri)!!

            return mDrawingParser.parseStream(stream)

        } catch (e: Exception) {
            e.printStackTrace()

            throw e

        } finally {
            stream?.close()
        }
    }

    fun saveChanges(drawing: DataDrawing, drawingUri: Uri): String {
        var stream: OutputStream? = null

        try {
            stream = context.contentResolver.openOutputStream(drawingUri)!!

            val serializedDrawing = mDrawingSerializer.serialize(drawing).toByteArray()

            stream.write(serializedDrawing)

            val filePath = File(drawingUri.path!!).absolutePath

            return filePath

        } catch (e: Exception) {
            e.printStackTrace()

            throw e

        } finally {
            stream?.close()
        }
    }

    fun saveNewFile(drawing: DataDrawing, filename: String): String {
        var stream: OutputStream? = null

        try {
            val filesDir = context.filesDir
            val drawingFile = File(filesDir.path + "/" + filename)

            drawingFile.createNewFile()

            val fileStream = FileOutputStream(drawingFile)
            val serializedDrawing = mDrawingSerializer.serialize(drawing).toByteArray()

            fileStream.write(serializedDrawing)

            return drawingFile.absolutePath

        } catch (e: Exception) {
            e.printStackTrace()

            throw e

        } finally {
            stream?.close()
        }
    }
}