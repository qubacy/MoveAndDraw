package com.qubacy.moveanddraw._common.error

enum class ErrorEnum(val id: Long) {
    ACCELEROMETER_UNAVAILABLE(0),

    // File-related:

    WRONG_FILE_TYPE(100),
    NO_FILE_LOADED(101),


}