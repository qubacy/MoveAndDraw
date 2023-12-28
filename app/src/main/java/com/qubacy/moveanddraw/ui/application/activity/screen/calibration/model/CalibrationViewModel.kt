package com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qubacy.moveanddraw._common.util.struct.takequeue.TakeQueue
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain.calibration.CalibrationUseCase
import com.qubacy.moveanddraw.ui.application.MoveAndDrawApplication
import com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model.state.CalibrationUiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.model.AccelerometerViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class CalibrationViewModel @Inject constructor(
    private val mCalibrationUseCase: CalibrationUseCase
) : AccelerometerViewModel<CalibrationUiState>(mCalibrationUseCase) {
    companion object {
        const val CALIBRATING_DURATION = 5000L
    }

    fun startCalibration() {
        mUiState.value = CalibrationUiState(CalibrationUiState.State.CALIBRATING)

        viewModelScope.launch(Dispatchers.IO) {
            delay(CALIBRATING_DURATION)
            onCalibrationEnded()
        }
    }

    private fun onCalibrationEnded() {
        viewModelScope.launch(Dispatchers.Main) {
            mUiState.value = CalibrationUiState(CalibrationUiState.State.CALIBRATED)
        }
    }

    override fun getUiStateWithUiOperation(uiOperation: UiOperation): CalibrationUiState {
        val state = mUiState.value?.state ?: CalibrationUiState.State.IDLE

        return CalibrationUiState(state = state, pendingOperations = TakeQueue(uiOperation))
    }
}

@Module
@InstallIn(ViewModelComponent::class)
object CalibrationViewModelModule {
    @Provides
    fun provideCalibrationUseCase(
        @ApplicationContext context: Context
    ): CalibrationUseCase {
        val errorDataRepository = ErrorDataRepository((context as MoveAndDrawApplication).db.errorDao())

        return CalibrationUseCase(errorDataRepository)
    }
}