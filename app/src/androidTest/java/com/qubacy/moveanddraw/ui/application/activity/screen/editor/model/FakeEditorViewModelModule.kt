package com.qubacy.moveanddraw.ui.application.activity.screen.editor.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qubacy.moveanddraw.data.drawing.repository.DrawingDataRepository
import com.qubacy.moveanddraw.data.drawing.repository.source.local.LocalDrawingDataSource
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain.editor.EditorUseCase
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment._common.model.business.FakeBusinessViewModelModule
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.model.state.EditorUiState
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito

@Module
@TestInstallIn(
    components = [ActivityRetainedComponent::class],
    replaces = [EditorViewModelFactoryModule::class]
)
object FakeEditorViewModelModule {
    class FakeEditorViewModelFactory(
    ) : FakeBusinessViewModelModule.BusinessViewModelFactory<EditorUiState>() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val viewModelMock = Mockito.mock(EditorViewModel::class.java)
            val uiState = MutableLiveData<EditorUiState>()

            Mockito.`when`(viewModelMock.uiState).thenReturn(uiState)
//            Mockito.`when`(viewModelMock.retrieveError(Mockito.anyLong()))
//                .thenAnswer {
//                    uiState.value = EditorUiState(
//                        pendingOperations = TakeQueue(ShowErrorUiOperation(TEST_ERROR))
//                    )
//
//                    Unit
//                }

            return viewModelMock as T
        }

        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return super.create(key, modelClass, handle)
        }
    }

    @Provides
    @EditorViewModelFactoryQualifier
    fun provideEditorViewModelFactory(
    ): ViewModelProvider.Factory {
        return FakeEditorViewModelFactory()
    }

    @Provides
    fun provideEditorUseCase(
        errorDataRepository: ErrorDataRepository,
        @ApplicationContext context: Context
    ): EditorUseCase {
        val drawingDataRepository = DrawingDataRepository(LocalDrawingDataSource(context))

        return EditorUseCase(errorDataRepository, drawingDataRepository)
    }
}