package com.qubacy.moveanddraw.ui.application.activity.screen.editor.model._test.data

import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.domain.editor.result.AddNewFaceToDrawingResult
import com.qubacy.moveanddraw.domain.editor.result.RemoveLastFaceFromDrawingResult
import com.qubacy.moveanddraw.domain.editor.result.SaveDrawingResult
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing.model._test.data.DrawingMockUseCaseInitData

class EditorUseCaseMockInitData(
    loadedDrawing: Drawing? = null,
    val saveDrawingResult: SaveDrawingResult? = null,
    val removeLastFaceFromDrawingResult: RemoveLastFaceFromDrawingResult? = null,
    val addNewFaceToDrawingResult: AddNewFaceToDrawingResult? = null
) : DrawingMockUseCaseInitData(loadedDrawing) {

}