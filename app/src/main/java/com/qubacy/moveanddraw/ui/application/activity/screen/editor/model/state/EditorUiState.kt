package com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state

import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.DrawingUiState

class EditorUiState(
    drawing: Drawing? = null,
    isLoading: Boolean = false,
    pendingOperations: TakeQueue<UiOperation> = TakeQueue()
) : DrawingUiState(drawing, isLoading, pendingOperations) {

}