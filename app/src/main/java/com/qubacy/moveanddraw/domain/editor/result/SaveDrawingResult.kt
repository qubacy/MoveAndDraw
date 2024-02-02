package com.qubacy.moveanddraw.domain.editor.result

import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.domain._common.usecase._common.result._common.Result

class SaveDrawingResult(
    val drawing: Drawing,
    val filePath: String
) : Result {

}