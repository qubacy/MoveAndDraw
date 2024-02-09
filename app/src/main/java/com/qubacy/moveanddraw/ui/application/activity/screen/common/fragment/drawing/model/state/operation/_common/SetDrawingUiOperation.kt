package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.operation._common

import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation

open class SetDrawingUiOperation(
    val drawing: Drawing
) : UiOperation {

}