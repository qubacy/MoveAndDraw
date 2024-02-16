package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common

import android.os.Parcelable
import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation

// TODO: UI Operations concept is certainly redundant. The same could be done by using separate
//  LiveData & Flow objects instead of just having common one. So there's no need to keep this field
//  and handle the whole spectre of operations. It's too sophisticated for the thing of that kind;
abstract class UiState(
    val pendingOperations: TakeQueue<UiOperation> = TakeQueue() // todo: should I preserve it as well?;
) : Parcelable {

}