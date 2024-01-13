package com.qubacy.moveanddraw.domain.editor

import com.qubacy.moveanddraw.data.drawing.repository.DrawingDataRepository
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.usecase.drawing.DrawingUseCase
import javax.inject.Inject

class EditorUseCase @Inject constructor(
    errorDataRepository: ErrorDataRepository,
    drawingDataRepository: DrawingDataRepository
) : DrawingUseCase(
    errorDataRepository,
    drawingDataRepository
) {

}