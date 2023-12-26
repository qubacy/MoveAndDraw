package com.qubacy.moveanddraw.domain._common.usecase

import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.usecase.result._common.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class UseCase(
    protected val mErrorDataRepository: ErrorDataRepository,
    protected var mCoroutineScope: CoroutineScope = GlobalScope,
    protected var mCoroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    protected val mResultFlow: MutableStateFlow<Result?> = MutableStateFlow(null)
    val resultFlow: StateFlow<Result?> = mResultFlow

    fun setCoroutineScope(coroutineScope: CoroutineScope) {
        mCoroutineScope = coroutineScope
    }

    fun setCoroutineDispatcher(coroutineDispatcher: CoroutineDispatcher) {
        mCoroutineDispatcher = coroutineDispatcher
    }
}