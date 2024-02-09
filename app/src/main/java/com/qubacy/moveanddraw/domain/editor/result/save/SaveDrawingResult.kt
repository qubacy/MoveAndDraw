package com.qubacy.moveanddraw.domain.editor.result.save

import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.domain._common.usecase.drawing.result._common.SetDrawingResult

class SaveDrawingResult(
    drawing: Drawing,
    val filePath: String
) : SetDrawingResult(drawing) {

}