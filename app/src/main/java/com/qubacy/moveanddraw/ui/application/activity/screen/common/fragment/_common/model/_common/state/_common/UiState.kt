package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common

import android.os.Parcelable
import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation

abstract class UiState(
    val pendingOperations: TakeQueue<UiOperation> = TakeQueue() // todo: should I preserve it as well?;
) : Parcelable {

}