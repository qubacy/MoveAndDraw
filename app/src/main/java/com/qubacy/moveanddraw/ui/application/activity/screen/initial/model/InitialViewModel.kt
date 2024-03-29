package com.qubacy.moveanddraw.ui.application.activity.screen.initial.model

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.data.preview.repository.PreviewDataRepository
import com.qubacy.moveanddraw.data.preview.repository.source.local.LocalPreviewDataSource
import com.qubacy.moveanddraw.domain._common.usecase._common.result._common.Result
import com.qubacy.moveanddraw.domain.initial.InitialUseCase
import com.qubacy.moveanddraw.domain.initial.result.GetExamplePreviewsResult
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
    savedStateHandle: SavedStateHandle,
    private val mInitialUseCase: InitialUseCase
) : BusinessViewModel<InitialUiState>(savedStateHandle, mInitialUseCase) {
    open fun getExampleDrawingPreviews() {
        mInitialUseCase.getExamplePreviews()
    }

    override fun getUiStateWithUiOperation(uiOperation: UiOperation): InitialUiState {
        return InitialUiState(
            uiState.value?.previewUris ?: listOf(),
            TakeQueue(uiOperation)
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
        errorDataRepository: ErrorDataRepository,
        @ApplicationContext context: Context
    ): InitialUseCase {
        val previewDataRepository = PreviewDataRepository(LocalPreviewDataSource(context))

        return InitialUseCase(errorDataRepository, previewDataRepository)
    }
}

