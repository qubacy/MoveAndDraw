package com.qubacy.moveanddraw.data.drawing.repository

import android.net.Uri
import com.qubacy.moveanddraw.data._common.repository._common.DataRepository
import com.qubacy.moveanddraw.data.drawing.model.DataDrawing
import com.qubacy.moveanddraw.data.drawing.repository.result.save._common.SaveDrawingResult
import com.qubacy.moveanddraw.data.drawing.repository.source.local.LocalDrawingDataSource
import javax.inject.Inject

class DrawingDataRepository @Inject constructor(
    private val mLocalDrawingDataSource: LocalDrawingDataSource
) : DataRepository() {
    fun loadDrawing(drawingUri: Uri): DataDrawing {
        return mLocalDrawingDataSource.load(drawingUri)
    }

    fun saveDrawing(drawing: DataDrawing, drawingUri: Uri): SaveDrawingResult {
        var gottenDrawingFilePath: String? = null
        var gottenDrawingUri: Uri = drawingUri

        try {
            gottenDrawingFilePath = mLocalDrawingDataSource.saveChanges(drawing, drawingUri)

        } catch (e: Exception) {
            val filename = mLocalDrawingDataSource.getDrawingFileNameByUri(drawingUri)
            val result = saveNewDrawing(drawing, filename)

            gottenDrawingFilePath = result.filePath
            gottenDrawingUri = result.uri
        }

        return SaveDrawingResult(gottenDrawingFilePath!!, gottenDrawingUri)
    }

    fun saveNewDrawing(drawing: DataDrawing, filename: String): SaveDrawingResult {
        val result = mLocalDrawingDataSource.saveNewFile(drawing, filename)

        return SaveDrawingResult(result.filePath, result.uri)
    }
}