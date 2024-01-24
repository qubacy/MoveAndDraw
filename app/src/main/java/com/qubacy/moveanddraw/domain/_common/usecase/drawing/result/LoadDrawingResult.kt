package com.qubacy.moveanddraw.domain._common.usecase.drawing.result

import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.domain._common.usecase._common.result._common.Result

class LoadDrawingResult(
    val drawing: Drawing
) : Result {

}