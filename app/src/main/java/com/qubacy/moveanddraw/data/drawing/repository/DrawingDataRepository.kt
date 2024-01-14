package com.qubacy.moveanddraw.data.drawing.repository

import android.net.Uri
import com.qubacy.moveanddraw.data._common.repository._common.DataRepository
import com.qubacy.moveanddraw.data.drawing.model.DataDrawing
import com.qubacy.moveanddraw.data.drawing.repository.source.local.LocalDrawingDataSource
import javax.inject.Inject

class DrawingDataRepository @Inject constructor(
    private val mLocalDrawingDataSource: LocalDrawingDataSource
) : DataRepository() {
    fun loadDrawing(drawingUri: Uri): DataDrawing {
        return mLocalDrawingDataSource.load(drawingUri)
    }

    fun saveDrawing(drawing: DataDrawing, drawingUri: Uri): String {
        return mLocalDrawingDataSource.saveChanges(drawing, drawingUri)
    }

    fun saveNewDrawing(drawing: DataDrawing, filename: String): String {
        return mLocalDrawingDataSource.saveNewFile(drawing, filename)
    }
}