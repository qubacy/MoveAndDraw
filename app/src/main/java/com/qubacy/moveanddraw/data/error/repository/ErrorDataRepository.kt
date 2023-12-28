package com.qubacy.moveanddraw.data.error.repository

import com.qubacy.moveanddraw._common.error.Error
import com.qubacy.moveanddraw.data._common.repository._common.DataRepository
import com.qubacy.moveanddraw.data.error.repository.source.local.LocalErrorDataSource
import com.qubacy.moveanddraw.data.error.repository.source.local.model.toError
import java.util.Locale
import javax.inject.Inject

open class ErrorDataRepository @Inject constructor(
    private val mLocalErrorDataSource: LocalErrorDataSource
) : DataRepository() {
    open fun getError(id: Long): Error {
        val lang = Locale.getDefault().language

        return mLocalErrorDataSource.getErrorById(id, lang)?.toError()
            ?: throw IllegalStateException()
    }
}