package com.qubacy.moveanddraw._common.error

data class Error(
    val id: Long,
    val message: String,
    val isCritical: Boolean
) {

}