package com.qubacy.moveanddraw.domain._common.usecase._common.result.error

import com.qubacy.moveanddraw._common.error.Error
import com.qubacy.moveanddraw.domain._common.usecase._common.result._common.Result

class ErrorResult(
    val error: Error
) : Result {

}