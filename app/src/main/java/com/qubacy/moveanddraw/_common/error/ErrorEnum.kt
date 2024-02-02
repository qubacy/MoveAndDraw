package com.qubacy.moveanddraw._common.error

enum class ErrorEnum(val id: Long) {
    ACCELEROMETER_UNAVAILABLE(0),

    // File-related:

    WRONG_FILE_TYPE(100),
    NO_FILE_LOADED(101),

    // Canvas-related:

    NULL_FACE_SKETCH(200),
    NULL_CURRENT_DRAWING(201),
    INVALID_DRAWING(202),
    INVALID_DRAWING_FILENAME(203),
}