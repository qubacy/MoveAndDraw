package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.qubacy.moveanddraw.domain._common.usecase.UseCase
import com.qubacy.moveanddraw.domain._common.usecase.result._common.Result
import com.qubacy.moveanddraw.domain._common.usecase.result.error.ErrorResult
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.BaseViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.UiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation.error.ShowErrorUiOperation
import kotlinx.coroutines.flow.map

abstract class BusinessViewModel<UiStateType : UiState>(
    useCase: UseCase
) : BaseViewModel<UiStateType>() {
    override val uiState: LiveData<UiStateType?> =
        useCase.resultFlow.map { updateUiStateWithResult(it) }.asLiveData()

    protected fun updateUiStateWithResult(result: Result?): UiStateType? {
        if (result == null) return null

        return when (result::class) {
            ErrorResult::class -> processErrorResult(result as ErrorResult)
            else -> processResult(result)
        }
    }

    protected open fun processErrorResult(errorResult: ErrorResult): UiStateType {
        return getUiStateWithUiOperation(ShowErrorUiOperation(errorResult.error))
    }

    protected open fun processResult(result: Result): UiStateType? = null
    protected abstract fun getUiStateWithUiOperation(uiOperation: UiOperation): UiStateType
}