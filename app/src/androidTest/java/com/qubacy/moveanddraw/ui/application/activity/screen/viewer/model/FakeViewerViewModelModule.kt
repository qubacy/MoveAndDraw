package com.qubacy.moveanddraw.ui.application.activity.screen.viewer.model

import android.content.Context
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qubacy.moveanddraw.data.drawing.repository.DrawingDataRepository
import com.qubacy.moveanddraw.data.drawing.repository.source.local.LocalDrawingDataSource
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain.viewer.ViewerUseCase
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment._common.model.business.FakeBusinessViewModelModule
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.UiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business.BusinessViewModel
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

    ) : FakeBusinessViewModelModule.BusinessViewModelFactory<ViewerUiState>() {
        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return super.create(key, modelClass, handle)
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