package com.qubacy.moveanddraw.domain.calibration

import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.usecase.UseCase
import javax.inject.Inject

open class CalibrationUseCase @Inject constructor(
    errorDataRepository: ErrorDataRepository
) : UseCase(errorDataRepository) {

}