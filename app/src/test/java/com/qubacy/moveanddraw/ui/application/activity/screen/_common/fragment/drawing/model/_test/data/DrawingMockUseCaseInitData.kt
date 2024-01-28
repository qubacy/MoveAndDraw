package com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment.drawing.model._test.data

import com.qubacy.moveanddraw._common._test.data.InitData
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing

open class DrawingMockUseCaseInitData(
    val loadedDrawing: Drawing? = null
) : InitData {
}