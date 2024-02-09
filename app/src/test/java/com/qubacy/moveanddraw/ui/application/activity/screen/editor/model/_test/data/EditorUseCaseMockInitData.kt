package com.qubacy.moveanddraw.ui.application.activity.screen.editor.model._test.data

import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.domain.editor.result.face.add.AddNewFaceToDrawingResult
import com.qubacy.moveanddraw.domain.editor.result.face.remove.RemoveLastFaceFromDrawingResult
import com.qubacy.moveanddraw.domain.editor.result.save.SaveDrawingResult
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing.model._test.data.DrawingMockUseCaseInitData

class EditorUseCaseMockInitData(
    loadedDrawing: Drawing? = null,
    val saveDrawingResult: SaveDrawingResult? = null,
    val removeLastFaceFromDrawingResult: RemoveLastFaceFromDrawingResult? = null,
    val addNewFaceToDrawingResult: AddNewFaceToDrawingResult? = null
) : DrawingMockUseCaseInitData(loadedDrawing) {

}