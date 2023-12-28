package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.model

import com.qubacy.moveanddraw.domain._common.usecase.UseCase
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.UiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business.BusinessViewModel

abstract class AccelerometerViewModel<UiStateType : UiState>(
    useCase: UseCase
) : BusinessViewModel<UiStateType>(useCase) {
    private var mXLastOffset: Float = 0f
    val xLastOffset get() = mXLastOffset

    private var mYLastOffset: Float = 0f
    val yLastOffset get() = mYLastOffset

    private var mZLastOffset: Float = 0f
    val zLastOffset get() = mZLastOffset

    fun setLastOffsets(xOffset: Float, yOffset: Float, zOffset: Float) {
        mXLastOffset = xOffset
        mYLastOffset = yOffset
        mZLastOffset = zOffset
    }
}