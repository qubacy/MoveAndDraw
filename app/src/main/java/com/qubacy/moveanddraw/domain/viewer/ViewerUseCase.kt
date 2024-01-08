package com.qubacy.moveanddraw.domain.viewer

import com.qubacy.moveanddraw.data.drawing.repository.DrawingDataRepository
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.usecase.drawing.DrawingUseCase
import javax.inject.Inject

open class ViewerUseCase @Inject constructor(
    errorDataRepository: ErrorDataRepository,
    drawingDataRepository: DrawingDataRepository
) : DrawingUseCase(
    errorDataRepository,
    drawingDataRepository
) {

}