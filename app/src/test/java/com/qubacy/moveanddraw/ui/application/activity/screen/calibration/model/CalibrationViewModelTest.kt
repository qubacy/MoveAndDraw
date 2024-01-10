package com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model

import com.qubacy.moveanddraw._common.data.InitData
import com.qubacy.moveanddraw._common.util.livedata.getOrAwaitValue
import com.qubacy.moveanddraw.domain.calibration.CalibrationUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import com.qubacy.moveanddraw.ui.application.activity.screen._common.fragment._common.model.BusinessViewModelTest
import com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model.state.CalibrationUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

// Warning! Launching the test suit can lead to some exceptions. BUT every case is CORRECT by itself;
class CalibrationViewModelTest :
    BusinessViewModelTest<CalibrationUiState, CalibrationUseCase, CalibrationViewModel>() {

    override fun mockUseCase(initData: InitData?): CalibrationUseCase {
        return Mockito.mock(CalibrationUseCase::class.java)
    }

    override fun createViewModel(useCaseMock: CalibrationUseCase): CalibrationViewModel {
        return CalibrationViewModel(useCaseMock)
    }

    @Before
    override fun setup() {
        super.setup()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun executeCalibrationTest() = mainCoroutineRule.run {
        initViewModel()

//        var uiState = mViewModel.uiState.getOrAwaitValue()!!
//
//        Assert.assertEquals(CalibrationUiState.State.IDLE, uiState.state)

        mViewModel.startCalibration()

        var uiState = mViewModel.uiState.getOrAwaitValue()!!

        Assert.assertEquals(CalibrationUiState.State.CALIBRATING, uiState.state)

        runBlocking { delay(CalibrationViewModel.CALIBRATING_DURATION) }

        uiState = mViewModel.uiState.getOrAwaitValue()!!

        Assert.assertEquals(CalibrationUiState.State.CALIBRATED, uiState.state)
    }
}