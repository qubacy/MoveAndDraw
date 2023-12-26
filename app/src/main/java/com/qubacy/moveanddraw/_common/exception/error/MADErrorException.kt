package com.qubacy.moveanddraw._common.exception.error

import com.qubacy.moveanddraw._common.exception._common.MADException

class MADErrorException(
    val errorId: Long
) : MADException() {

}