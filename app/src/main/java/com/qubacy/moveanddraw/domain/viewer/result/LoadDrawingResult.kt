package com.qubacy.moveanddraw.domain.viewer.result

import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing
import com.qubacy.moveanddraw.domain._common.usecase._common.result._common.Result

class LoadDrawingResult(
    val drawing: Drawing
) : Result {

}