package com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qubacy.moveanddraw.data.drawing.repository.DrawingDataRepository
import com.qubacy.moveanddraw.data.drawing.repository.source.local.LocalDrawingDataSource
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain.viewer.ViewerUseCase
import com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model.state.ViewerUiState
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito

@Module
@TestInstallIn(
    components = [ActivityRetainedComponent::class],
    replaces = [ViewerViewModelFactoryModule::class]
)
object FakeViewerViewModelModule {
    class FakeViewerViewModelFactory(
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val viewModelMock = Mockito.mock(ViewerViewModel::class.java)

            Mockito.`when`(viewModelMock.uiState)
                .thenReturn(MutableLiveData<ViewerUiState>())

            return viewModelMock as T
        }
    }

    @Provides
    @ViewerViewModelFactoryQualifier
    fun provideViewerViewModelFactory(
    ): ViewModelProvider.Factory {
        return FakeViewerViewModelFactory()
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