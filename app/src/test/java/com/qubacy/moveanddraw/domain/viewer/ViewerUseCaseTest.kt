package com.qubacy.moveanddraw.domain.viewer

import com.qubacy.moveanddraw.data.drawing.repository.DrawingDataRepository
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.usecase.drawing.DrawingUseCaseTest
import com.qubacy.moveanddraw._common._test.data.InitData

class ViewerUseCaseTest(

) : DrawingUseCaseTest<ViewerUseCase>() {
    override fun generateDrawingUseCase(
        errorDataRepositoryMock: ErrorDataRepository,
        drawingDataRepositoryMock: DrawingDataRepository,
        initData: InitData?
    ): ViewerUseCase {
        return ViewerUseCase(errorDataRepositoryMock, drawingDataRepositoryMock)
    }


}