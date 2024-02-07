package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.usecase._common.UseCase
import com.qubacy.moveanddraw.domain._common.usecase._common.result._common.Result
import com.qubacy.moveanddraw.domain._common.usecase._common.result.error.ErrorResult
import com.qubacy.moveanddraw.ui.application.MoveAndDrawApplication
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.BaseViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.UiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation.error.ShowErrorUiOperation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

abstract class BusinessViewModel<UiStateType : UiState>(
    protected val mSavedStateHandle: SavedStateHandle,
    private val mUseCase: UseCase
) : BaseViewModel<UiStateType>() {
    companion object {
        const val UI_STATE_KEY = "uiState"
    }

    /**
     * This field is formed according to the results of a business logic working;
     */
    val uiStateFlow = mUseCase.resultFlow.filterNotNull().map { updateUiStateWithResult(it) }
    override val mUiState = uiStateFlow.asLiveData() as MutableLiveData<UiStateType?>

    init {
        mUseCase.setCoroutineScope(viewModelScope)

        mUiState.value = mSavedStateHandle.get<UiStateType?>(UI_STATE_KEY)
    }

    override fun onCleared() {
        mSavedStateHandle[UI_STATE_KEY] = mUiState.value

        super.onCleared()
    }

    open fun retrieveError(errorId: Long) {
        mUseCase.retrieveError(errorId)
    }

    protected fun updateUiStateWithResult(result: Result): UiStateType? {
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

@Module
@InstallIn(ActivityRetainedComponent::class)
object BusinessViewModelModule {
    @Provides
    fun provideErrorDataRepository(
        @ApplicationContext context: Context
    ): ErrorDataRepository {
        return ErrorDataRepository((context as MoveAndDrawApplication).db.errorDao())
    }
}