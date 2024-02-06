package com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state

import android.os.Parcel
import android.os.Parcelable
import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state.DrawingUiState

class EditorUiState : DrawingUiState {
    constructor(parcel: Parcel) : super(parcel) {

    }

    constructor(
        isLoading: Boolean = false,
        drawing: Drawing? = null,
        pendingOperations: TakeQueue<UiOperation> = TakeQueue()
    ) : super(isLoading, drawing, pendingOperations) {

    }

    companion object CREATOR : Parcelable.Creator<EditorUiState> {
        override fun createFromParcel(parcel: Parcel): EditorUiState {
            return EditorUiState(parcel)
        }

        override fun newArray(size: Int): Array<EditorUiState?> {
            return arrayOfNulls(size)
        }
    }

}