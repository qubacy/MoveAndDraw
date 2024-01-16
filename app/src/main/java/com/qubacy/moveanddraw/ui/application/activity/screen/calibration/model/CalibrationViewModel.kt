package com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qubacy.moveanddraw._common.util.struct.takequeue._common.TakeQueue
import com.qubacy.moveanddraw.data.error.repository.ErrorDataRepository
import com.qubacy.moveanddraw.domain.calibration.CalibrationUseCase
import com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model.state.CalibrationUiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.operation._common.UiOperation
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business.BusinessViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.model._common.AccelerometerStateHolder
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.model.simple.SimpleAccelerometerStateHolder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.util.Timer
import javax.inject.Inject
import javax.inject.Qualifier
import kotlin.concurrent.schedule

@HiltViewModel
open class CalibrationViewModel @Inject constructor(
    private val mCalibrationUseCase: CalibrationUseCase
) : BusinessViewModel<CalibrationUiState>(mCalibrationUseCase),
    AccelerometerStateHolder by SimpleAccelerometerStateHolder() {
    companion object {
        const val CALIBRATING_DURATION = 5000L
    }

    private var mXAverageOffset: Float = 0f
    private var mYAverageOffset: Float = 0f
    private var mZAverageOffset: Float = 0f

    val xAverageOffset get() = mXAverageOffset
    val yAverageOffset get() = mYAverageOffset
    val zAverageOffset get() = mZAverageOffset

    private var mXTotalOffset: Float = 0f
    private var mYTotalOffset: Float = 0f
    private var mZTotalOffset: Float = 0f

    private var mUpdateCount: Int = 0

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

    override fun applyAccelerations(
        xAcceleration: Float, yAcceleration: Float, zAcceleration: Float
    ) {
        mUpdateCount += 1

        val offsets = getOffsetsByAccelerations(xAcceleration, yAcceleration, zAcceleration)

        updateTotalOffsets(offsets)
        updateAverageOffsetsValues()
    }

    private fun updateAverageOffsetsValues() {
        mXAverageOffset = mXTotalOffset / mUpdateCount
        mYAverageOffset = mYTotalOffset / mUpdateCount
        mZAverageOffset = mZTotalOffset / mUpdateCount
    }

    private fun updateTotalOffsets(offsets: FloatArray) {
        mXTotalOffset += offsets[0]
        mYTotalOffset += offsets[1]
        mZTotalOffset += offsets[2]
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

@Qualifier
annotation class CalibrationViewModelFactoryQualifier

@Module
@InstallIn(ActivityRetainedComponent::class)
object CalibrationViewModelFactoryModule {
    @Provides
    @CalibrationViewModelFactoryQualifier
    fun provideCalibrationViewModelFactory(
        calibrationUseCase: CalibrationUseCase
    ): ViewModelProvider.Factory {
        return CalibrationViewModelFactory(calibrationUseCase)
    }

    @Provides
    fun provideCalibrationUseCase(
        errorDataRepository: ErrorDataRepository
    ): CalibrationUseCase {
        return CalibrationUseCase(errorDataRepository)
    }
}