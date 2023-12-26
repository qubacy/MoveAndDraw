package com.qubacy.moveanddraw._common.exception.error

import com.qubacy.moveanddraw._common.error.Error
import com.qubacy.moveanddraw._common.exception._common.MADException

class MADErrorException(
    val error: Error
) : MADException() {

}