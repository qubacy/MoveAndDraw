package com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.qubacy.moveanddraw.data._common.repository._common.source.local.TestDatabase
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain.calibration.CalibrationUseCase
import com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model.state.CalibrationUiState
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito

@Module
@TestInstallIn(
    components = [ActivityRetainedComponent::class],
    replaces = [CalibrationViewModelFactoryModule::class]
)
object FakeCalibrationViewModelFactoryModule {
    class FakeCalibrationViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val viewModelMock = Mockito.mock(CalibrationViewModel::class.java)

            Mockito.`when`(viewModelMock.uiState)
                .thenReturn(MutableLiveData<CalibrationUiState>())

            return viewModelMock as T
        }
    }

    @Provides
    fun provideCalibrationViewModelFactory(
    ): ViewModelProvider.Factory {
        return FakeCalibrationViewModelFactory()
    }

    @Provides
    fun provideCalibrationUseCase(
        @ApplicationContext context: Context
    ): CalibrationUseCase {
        val db = TestDatabase.getDatabase(InstrumentationRegistry.getInstrumentation().targetContext)

        val errorDataRepository = ErrorDataRepository(db.errorDao())

        return CalibrationUseCase(errorDataRepository)
    }
}