package com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model

import android.content.Context
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.data.drawing.repository.DrawingDataRepository
import com.qubacy.moveanddraw.data.drawing.repository.source.local.LocalDrawingDataSource
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.domain.viewer.ViewerUseCase
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.model.DrawingViewModel
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
    savedStateHandle: SavedStateHandle,
    private val mViewerUseCase: ViewerUseCase
) : DrawingViewModel<ViewerUiState>(savedStateHandle, mViewerUseCase) {
    override fun generateDrawingUiState(
        drawing: Drawing?,
        isLoading: Boolean,
        pendingOperations: TakeQueue<UiOperation>
    ): ViewerUiState {
        return ViewerUiState(drawing, isLoading, pendingOperations)
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
) : AbstractSavedStateViewModelFactory() {
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (!modelClass.isAssignableFrom(ViewerViewModel::class.java))
            throw IllegalArgumentException()

        return ViewerViewModel(handle, mViewerUseCase) as T
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
