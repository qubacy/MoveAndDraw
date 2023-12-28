package com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment._common.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.qubacy.moveanddraw._common.error.Error
import com.qubacy.moveanddraw._common.util.livedata.getOrAwaitValue
import com.qubacy.moveanddraw._common.util.rule.MainCoroutineRule
import com.qubacy.moveanddraw.domain._common.usecase.UseCase
import com.qubacy.moveanddraw.domain._common.usecase.result._common.Result
import com.qubacy.moveanddraw.domain._common.usecase.result.error.ErrorResult
import com.qubacy.moveanddraw.domain.calibration.CalibrationUseCase
import com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model.CalibrationViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.UiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation.error.ShowErrorUiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business.BusinessViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

abstract class BusinessViewModelTest<
    UiStateType : UiState, UseCaseType : UseCase, ViewModelType : BusinessViewModel<UiStateType>
>() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(Dispatchers.IO)

    protected lateinit var mResultFlow: MutableStateFlow<Result?>
    protected lateinit var mViewModel: ViewModelType

    protected abstract fun mockUseCase(): UseCaseType
    protected abstract fun createViewModel(useCaseMock: UseCaseType): ViewModelType

    @OptIn(ExperimentalCoroutinesApi::class)
    protected open fun initViewModel(
        error: Error? = null
    ) {
        mResultFlow = MutableStateFlow(null)

        val useCaseMock = mockUseCase()

        Mockito.`when`(useCaseMock.resultFlow)
            .thenReturn(mResultFlow)
        Mockito.`when`(useCaseMock.retrieveError(Mockito.anyLong()))
            .thenAnswer {
                mainCoroutineRule.launch {
                    mResultFlow.emit(ErrorResult(error!!))
                }
            }

        mViewModel = createViewModel(useCaseMock)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun retrieveErrorTest() = mainCoroutineRule.run {
        val error = Error(0, "test", false)

        initViewModel(error)

        mViewModel.retrieveError(error.id)

        val uiState = mViewModel.uiState.getOrAwaitValue()!!
        val operation = uiState.pendingOperations.take()!!

        Assert.assertEquals(ShowErrorUiOperation::class, operation::class)

        val gottenError = (operation as ShowErrorUiOperation).error

        Assert.assertEquals(error, gottenError)
    }
}