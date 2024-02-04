package com.qubacy.moveanddraw.data.drawing.repository.source.local

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.qubacy.moveanddraw._common.util.context.getFileNameByUri
import com.qubacy.moveanddraw._common.util.context.getFileProviderAuthority
import com.qubacy.moveanddraw.data._common.repository._common.source._common.DataSource
import com.qubacy.moveanddraw.data.drawing.model.DataDrawing
import com.qubacy.moveanddraw.data.drawing.repository.source.local.parser.obj.OBJDrawingParser
import com.qubacy.moveanddraw.data.drawing.repository.source.local.result.SaveNewFileResult
import com.qubacy.moveanddraw.data.drawing.repository.source.local.serializer.obj.OBJDrawingSerializer
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import javax.inject.Inject

open class LocalDrawingDataSource @Inject constructor(
    val context: Context
) : DataSource {
    companion object {
        const val FILE_URI_SCHEME = "file"
        const val CONTENT_URI_SCHEME = "content"
    }

    private val mDrawingParser = OBJDrawingParser()
    private val mDrawingSerializer = OBJDrawingSerializer()

    open fun load(drawingUri: Uri): DataDrawing {
        var stream: InputStream? = null

        try {
            stream = context.contentResolver.openInputStream(drawingUri)!!

            return mDrawingParser.parseStream(stream)

        }
        catch (e: Exception) { throw e }
        finally { stream?.close() }
    }

    private fun openContentStream(drawingUri: Uri): OutputStream {
        return context.contentResolver.openOutputStream(drawingUri)!!
    }

    private fun openFileStream(drawingUri: Uri): OutputStream {
        return FileOutputStream(drawingUri.path, false)
    }

    fun getFilesDir(): File {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!
    }

    open fun saveChanges(drawing: DataDrawing, drawingUri: Uri): String {
        var stream: OutputStream? = null

        try {
            stream = when (drawingUri.scheme) {
                FILE_URI_SCHEME -> openFileStream(drawingUri)
                CONTENT_URI_SCHEME -> openContentStream(drawingUri)
                else -> throw IllegalArgumentException()
            }

            val serializedDrawing = mDrawingSerializer.serialize(drawing).toByteArray()

            stream.write(serializedDrawing)

            val drawingFile = File(getFilesDir(), context.getFileNameByUri(drawingUri))

            return drawingFile.absolutePath

        }
        catch (e: Exception) { throw e }
        finally { stream?.close() }
    }

    open fun saveNewFile(drawing: DataDrawing, filename: String): SaveNewFileResult {
        var fileStream: FileOutputStream? = null

        try {
            val filesDir = getFilesDir()
            val drawingFile = File(filesDir.path + "/" + filename)

            drawingFile.createNewFile()

            fileStream = FileOutputStream(drawingFile)

            val serializedDrawing = mDrawingSerializer.serialize(drawing).toByteArray()

            fileStream.write(serializedDrawing)

            val contentUri = FileProvider.getUriForFile(
                context, context.getFileProviderAuthority(), drawingFile)

            return SaveNewFileResult(drawingFile.absolutePath, contentUri)

        }
        catch (e: Exception) { throw e }
        finally { fileStream?.close() }
    }

    fun getDrawingFileNameByUri(drawingUri: Uri): String {
        return context.getFileNameByUri(drawingUri)
    }
}