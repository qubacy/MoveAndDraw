package com.qubacy.moveanddraw.data.drawing.repository.source.local

import android.content.Context
import android.net.Uri
import com.qubacy.moveanddraw.data._common.repository._common.source._common.DataSource
import com.qubacy.moveanddraw.data.drawing.model.DataDrawing
import com.qubacy.moveanddraw.data.drawing.repository.source.local.parser.obj.OBJDrawingParser
import java.io.InputStream
import java.lang.Exception
import javax.inject.Inject

class LocalDrawingDataSource @Inject constructor(
    val context: Context
) : DataSource {
    private val mDrawingParser = OBJDrawingParser()

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

    fun save(drawing: DataDrawing) {
        // todo: Implement..


    }
}