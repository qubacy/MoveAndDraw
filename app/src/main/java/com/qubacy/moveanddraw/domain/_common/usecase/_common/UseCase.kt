package com.qubacy.moveanddraw.domain._common.usecase._common

import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.usecase._common.result._common.Result
import com.qubacy.moveanddraw.domain._common.usecase._common.result.error.ErrorResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

    protected suspend fun onErrorCaught(errorId: Long) {
        val error = mErrorDataRepository.getError(errorId)

        mResultFlow.emit(ErrorResult(error))
    }

    open fun retrieveError(errorId: Long) {
        mCoroutineScope.launch(mCoroutineDispatcher) {
            onErrorCaught(errorId)
        }
    }
}