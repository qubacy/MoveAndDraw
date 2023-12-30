package com.qubacy.moveanddraw.domain.viewer

import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.usecase.UseCase

open class ViewerUseCase(
    errorDataRepository: ErrorDataRepository
) : UseCase(
    errorDataRepository
) {

}