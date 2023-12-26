package com.qubacy.moveanddraw.data.error.repository

import com.qubacy.moveanddraw._common.error.Error
import com.qubacy.moveanddraw.data._common.repository._common.DataRepository
import com.qubacy.moveanddraw.data.error.repository.source.local.LocalErrorDataSource
import com.qubacy.moveanddraw.data.error.repository.source.local.model.toError
import javax.inject.Inject

class ErrorDataRepository @Inject constructor(
    private val mLocalErrorDataSource: LocalErrorDataSource
) : DataRepository() {
    fun getError(id: Long): Error {
        return mLocalErrorDataSource.getErrorById(id)?.toError()
            ?: throw IllegalStateException()
    }
}