package com.qubacy.moveanddraw.ui.application.activity.screen.initial.model.state

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.UiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation

class InitialUiState(
    val previewUris: List<Uri> = listOf(),
    pendingUiOperations: TakeQueue<UiOperation> = TakeQueue()
) : UiState(pendingUiOperations) {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(Uri.CREATOR) ?: listOf()
    ) {

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(previewUris)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<InitialUiState> {
        override fun createFromParcel(parcel: Parcel): InitialUiState {
            return InitialUiState(parcel)
        }

        override fun newArray(size: Int): Array<InitialUiState?> {
            return arrayOfNulls(size)
        }
    }
}