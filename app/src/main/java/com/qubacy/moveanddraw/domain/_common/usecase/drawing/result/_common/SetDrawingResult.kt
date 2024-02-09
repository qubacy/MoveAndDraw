package com.qubacy.moveanddraw.domain._common.usecase.drawing.result._common

import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.domain._common.usecase._common.result._common.Result

abstract class SetDrawingResult(
    val drawing: Drawing
) : Result {

}