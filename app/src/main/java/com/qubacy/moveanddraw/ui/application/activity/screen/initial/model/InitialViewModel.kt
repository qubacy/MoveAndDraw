package com.qubacy.moveanddraw.ui.application.activity.screen.initial.model

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.qubacy.moveanddraw._common.util.struct.takequeue.TakeQueue
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.data.preview.repository.PreviewDataRepository
import com.qubacy.moveanddraw.data.preview.repository.source.local.LocalPreviewDataSource
import com.qubacy.moveanddraw.domain._common.usecase.result._common.Result
import com.qubacy.moveanddraw.domain.initial.InitialUseCase
import com.qubacy.moveanddraw.domain.initial.result.GetExamplePreviewsResult
import com.qubacy.moveanddraw.ui.application.MoveAndDrawApplication
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business.BusinessViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.initial.model.state.InitialUiState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
open class InitialViewModel @Inject constructor(
    private val mInitialUseCase: InitialUseCase
) : BusinessViewModel<InitialUiState>(mInitialUseCase) {
    init {
        mInitialUseCase.setCoroutineScope(viewModelScope)
    }

    open fun getExampleDrawingPreviews() {
        mInitialUseCase.getExamplePreviews()
    }

    override fun getUiStateWithUiOperation(uiOperation: UiOperation): InitialUiState {
        return InitialUiState(
            uiState.value!!.previewUris,
            TakeQueue(uiState.value!!.pendingOperations, uiOperation)
        )
    }

    override fun processResult(result: Result): InitialUiState {
        return when (result::class) {
            GetExamplePreviewsResult::class ->
                processSetPreviewUrisOperation(result as GetExamplePreviewsResult)
            else -> {
                throw IllegalStateException()
            }
        }
    }

    private fun processSetPreviewUrisOperation(result: GetExamplePreviewsResult): InitialUiState {
        return InitialUiState(result.previewUris)
    }
}

@Module
@InstallIn(ViewModelComponent::class)
object InitialViewModelModule {
    @Provides
    fun provideInitialUseCase(
        context: Context
    ): InitialUseCase {
        val errorDataRepository = ErrorDataRepository((context as MoveAndDrawApplication).db.errorDao())
        val previewDataRepository = PreviewDataRepository(LocalPreviewDataSource(context))

        return InitialUseCase(errorDataRepository, previewDataRepository)
    }

    @Provides
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
}

