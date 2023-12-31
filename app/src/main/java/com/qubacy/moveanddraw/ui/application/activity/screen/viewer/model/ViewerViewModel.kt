package com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qubacy.moveanddraw._common.util.struct.takequeue.TakeQueue
import com.qubacy.moveanddraw.data.drawing.repository.DrawingDataRepository
import com.qubacy.moveanddraw.data.drawing.repository.source.local.LocalDrawingDataSource
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.usecase.result._common.Result
import com.qubacy.moveanddraw.domain._common.usecase.result.error.ErrorResult
import com.qubacy.moveanddraw.domain.viewer.ViewerUseCase
import com.qubacy.moveanddraw.domain.viewer.result.LoadDrawingResult
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation.error.ShowErrorUiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business.BusinessViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.state.ViewerUiState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Qualifier

@HiltViewModel
open class ViewerViewModel @Inject constructor(
    private val mViewerUseCase: ViewerUseCase
) : BusinessViewModel<ViewerUiState>(mViewerUseCase) {

    fun loadDrawing(drawingUri: Uri) {
        mViewerUseCase.loadDrawing(drawingUri)

        mUiState.value = ViewerUiState(isLoading = true)
    }

    override fun processResult(result: Result): ViewerUiState? {
        return when (result::class) {
            LoadDrawingResult::class -> { processLoadDrawingResult(result as LoadDrawingResult) }
            else -> null
        }
    }

    private fun processLoadDrawingResult(result: LoadDrawingResult): ViewerUiState {
        return ViewerUiState(drawing = result.drawing, isLoading = false)
    }

    override fun processErrorResult(errorResult: ErrorResult): ViewerUiState {
        return ViewerUiState(
            drawing = mUiState.value?.drawing,
            isLoading = false,
            pendingOperations = TakeQueue(ShowErrorUiOperation(errorResult.error))
        )
    }

    override fun getUiStateWithUiOperation(uiOperation: UiOperation): ViewerUiState {
        return ViewerUiState(
            drawing = mUiState.value?.drawing,
            pendingOperations = TakeQueue(uiOperation)
        )
    }
}

class ViewerViewModelFactory(
    private val mViewerUseCase: ViewerUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(ViewerViewModel::class.java))
            throw IllegalArgumentException()

        return ViewerViewModel(mViewerUseCase) as T
    }
}

@Qualifier
annotation class ViewerViewModelFactoryQualifier

@Module
@InstallIn(ActivityRetainedComponent::class)
object ViewerViewModelFactoryModule {
    @Provides
    @ViewerViewModelFactoryQualifier
    fun provideViewerViewModelFactory(
        viewerUseCase: ViewerUseCase
    ): ViewModelProvider.Factory {
        return ViewerViewModelFactory(viewerUseCase)
    }

    @Provides
    fun provideViewerUseCase(
        errorDataRepository: ErrorDataRepository,
        @ApplicationContext context: Context
    ): ViewerUseCase {
        val drawingDataRepository = DrawingDataRepository(LocalDrawingDataSource(context))

        return ViewerUseCase(errorDataRepository, drawingDataRepository)
    }
}
