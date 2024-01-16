package com.qubacy.moveanddraw.domain.editor.result

import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing
import com.qubacy.moveanddraw.domain._common.usecase._common.result._common.Result

class RemoveLastFaceFromDrawingResult(
    val drawing: Drawing
) : Result {

}