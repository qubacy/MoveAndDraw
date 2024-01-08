package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state

import com.qubacy.moveanddraw._common.util.struct.takequeue.TakeQueue
import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.UiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation

abstract class DrawingUiState(
    val drawing: Drawing? = null,
    val isLoading: Boolean = false,
    pendingOperations: TakeQueue<UiOperation> = TakeQueue()
) : UiState(pendingOperations) {

}