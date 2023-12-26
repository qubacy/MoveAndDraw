package com.qubacy.moveanddraw.data.error.repository

import com.qubacy.moveanddraw.data._common.repository._common.DataRepository
import com.qubacy.moveanddraw.data.error.model.DataError
import com.qubacy.moveanddraw.data.error.repository.source.local.LocalErrorDataSource
import javax.inject.Inject

class ErrorDataRepository @Inject constructor(
    private val mLocalErrorDataSource: LocalErrorDataSource
) : DataRepository() {
    fun getError(id: Long): DataError {
        return mLocalErrorDataSource.getErrorById(id) ?: throw IllegalStateException()
    }
}