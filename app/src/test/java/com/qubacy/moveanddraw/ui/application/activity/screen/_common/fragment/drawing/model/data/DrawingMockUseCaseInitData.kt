package com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing.model.data

import com.qubacy.moveanddraw._common.data.InitData
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing

data class DrawingMockUseCaseInitData(
    val loadedDrawing: Drawing
) : InitData {
}