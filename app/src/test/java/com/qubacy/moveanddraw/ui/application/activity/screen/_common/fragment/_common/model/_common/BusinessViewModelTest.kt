package com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment._common.model._common

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.qubacy.moveanddraw._common._test.data.InitData
import com.qubacy.moveanddraw._common.error.Error
import com.qubacy.moveanddraw._common._test.util.rule.MainCoroutineRule
import com.qubacy.moveanddraw.domain._common.usecase._common.UseCase
import com.qubacy.moveanddraw.domain._common.usecase._common.result._common.Result
import com.qubacy.moveanddraw.domain._common.usecase._common.result.error.ErrorResult
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.UiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation.error.ShowErrorUiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business.BusinessViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
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
    open val mainCoroutineRule = MainCoroutineRule(Dispatchers.Default)

    protected lateinit var mResultFlow: MutableStateFlow<Result?>
    protected lateinit var mUseCaseMock: UseCaseType
    protected lateinit var mViewModel: ViewModelType

    protected abstract fun mockUseCase(initData: InitData? = null): UseCaseType
    protected abstract fun createViewModel(
        savedStateHandleMock: SavedStateHandle,
        useCaseMock: UseCaseType
    ): ViewModelType

    @Before
    open fun setup() {

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    protected open fun initViewModel(
        error: Error? = null,
        useCaseMockInitData: InitData? = null
    ) {
        mResultFlow = MutableStateFlow(null)
        mUseCaseMock = mockUseCase(useCaseMockInitData)

        val savedStateHandleMock = Mockito.mock(SavedStateHandle::class.java)

        Mockito.`when`(mUseCaseMock.resultFlow).thenReturn(mResultFlow)
        Mockito.`when`(mUseCaseMock.retrieveError(Mockito.anyLong()))
            .thenAnswer {
                mainCoroutineRule.launch {
                    mResultFlow.emit(ErrorResult(error!!))
                }
            }

        mViewModel = createViewModel(savedStateHandleMock, mUseCaseMock)
    }

    @Test
    fun retrieveErrorTest() = runBlocking {
        val error = Error(0, "test", false)

        initViewModel(error)

        mViewModel.uiStateFlow.test {
            mViewModel.retrieveError(error.id)

            val uiState = awaitItem()!!
            val operation = uiState.pendingOperations.take()!!

            Assert.assertEquals(ShowErrorUiOperation::class, operation::class)

            val gottenError = (operation as ShowErrorUiOperation).error

            Assert.assertEquals(error, gottenError)
        }
    }
}