package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.state

import android.os.Parcel
import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.UiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation

abstract class DrawingUiState(
    val isLoading: Boolean = false,
    val drawing: Drawing? = null,
    pendingOperations: TakeQueue<UiOperation> = TakeQueue()
) : UiState(pendingOperations) {
    constructor(parcel: Parcel) : this(
        parcel.readByte().toInt() != 0,
        if (parcel.dataAvail() > 0) parcel.readParcelable<Drawing>(Drawing::class.java.classLoader)
        else null
    ) {

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(drawing, flags)
        parcel.writeByte(if (isLoading) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

}