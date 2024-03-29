package com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.operation.saved

import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.operation._common.SetDrawingUiOperation

class DrawingSavedUiOperation(
    drawing: Drawing,
    val filePath: String
) : SetDrawingUiOperation(drawing) {

}