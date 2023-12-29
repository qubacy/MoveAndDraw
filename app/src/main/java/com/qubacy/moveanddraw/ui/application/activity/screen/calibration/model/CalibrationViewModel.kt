package com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.schedule

@HiltViewModel
open class CalibrationViewModel @Inject constructor(
    private val mCalibrationUseCase: CalibrationUseCase
) : AccelerometerViewModel<CalibrationUiState>(mCalibrationUseCase) {
    companion object {
        const val CALIBRATING_DURATION = 5000L
    }

    fun startCalibration() {
        mUiState.value = CalibrationUiState(CalibrationUiState.State.CALIBRATING)

        Timer().schedule(CALIBRATING_DURATION) {
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

class CalibrationViewModelFactory(
    private val mCalibrationUseCase: CalibrationUseCase
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(CalibrationViewModel::class.java))
            throw IllegalArgumentException()

        return CalibrationViewModel(mCalibrationUseCase) as T
    }
}

//@Module
//@InstallIn(ViewModelComponent::class)
//object CalibrationViewModelModule {
//    @Provides
//    fun provideCalibrationUseCase(
//        @ApplicationContext context: Context
//    ): CalibrationUseCase {
//        val errorDataRepository = ErrorDataRepository((context as MoveAndDrawApplication).db.errorDao())
//
//        return CalibrationUseCase(errorDataRepository)
//    }
//}

@Module
@InstallIn(ActivityRetainedComponent::class)
object CalibrationViewModelFactoryModule {
    @Provides
    fun provideCalibrationViewModelFactory(
        calibrationUseCase: CalibrationUseCase
    ): ViewModelProvider.Factory {//CalibrationViewModelFactory {
        return CalibrationViewModelFactory(calibrationUseCase)
    }

    @Provides
    fun provideCalibrationUseCase(
        @ApplicationContext context: Context
    ): CalibrationUseCase {
        val errorDataRepository = ErrorDataRepository((context as MoveAndDrawApplication).db.errorDao())

        return CalibrationUseCase(errorDataRepository)
    }
}