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
}