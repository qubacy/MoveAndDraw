package com.qubacy.moveanddraw.ui.application.activity.screen.initial.model.state

import android.net.Uri
import com.qubacy.moveanddraw._common.util.struct.takequeue.TakeQueue
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.UiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation

class InitialUiState(
    val previewUris: List<Uri>,
    pendingUiOperations: TakeQueue<UiOperation> = TakeQueue()
) : UiState(pendingUiOperations) {

}