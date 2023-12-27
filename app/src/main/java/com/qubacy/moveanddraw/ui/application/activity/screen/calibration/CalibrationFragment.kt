package com.qubacy.moveanddraw.ui.application.activity.screen.calibration

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import com.google.android.material.transition.MaterialSharedAxis
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw.databinding.FragmentCalibrationBinding
import com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model.CalibrationViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model.state.CalibrationUiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.transition.DefaultSharedAxisTransitionGenerator
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IllegalStateException

@AndroidEntryPoint
class CalibrationFragment
    : BaseFragment<CalibrationUiState, CalibrationViewModel>(), SensorEventListener {
    companion object {
        const val TAG = "CALIBR_FRAGMENT"
    }

    override val mModel: CalibrationViewModel by viewModels()

    private lateinit var mBinding: FragmentCalibrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = DefaultSharedAxisTransitionGenerator.generate(
            requireContext(),
            MaterialSharedAxis.Z,
            true
        )
        DefaultSharedAxisTransitionGenerator.generate(
            requireContext(),
            MaterialSharedAxis.Z,
            false
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentCalibrationBinding.inflate(inflater, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.fragmentCalibrationButtonStart.setOnClickListener { onStartClicked() }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onDestroy() {
        endSensorListening()

        super.onDestroy()
    }

    private fun onStartClicked() {
        when (mModel.uiState.value!!.state) {
            CalibrationUiState.State.IDLE -> { startCalibration() }
            CalibrationUiState.State.CALIBRATED -> { goToEditor() }
            else -> throw IllegalStateException()
        }
    }

    private fun startCalibration() {
        mModel.startCalibration()
    }

    private fun startCalibrationWithSensor() {
        val sensorManager = getSystemService(requireContext(), SensorManager::class.java)
                as SensorManager
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        if (sensor == null) return //mModel.retrieveError() // todo: implement;

        sensorManager.registerListener(this, sensor, SENSOR_DELAY_NORMAL)
    }

    private fun endSensorListening() {
        val sensorManager = getSystemService(requireContext(), SensorManager::class.java)
                as SensorManager

        sensorManager.unregisterListener(this)
    }

    private fun goToEditor() {
        // todo: implement..


    }

    private fun changeProgressIndicatorEnabled(isEnabled: Boolean) {
        mBinding.fragmentCalibrationProgressIndicator.visibility =
            if (isEnabled) View.VISIBLE else View.GONE
    }

    private fun changeStartButtonEnabled(isEnabled: Boolean) {
        mBinding.fragmentCalibrationButtonStart.isEnabled = isEnabled
    }

    override fun setUiElementsState(uiState: CalibrationUiState) {
        when (uiState.state) {
            CalibrationUiState.State.IDLE -> {}
            CalibrationUiState.State.CALIBRATING -> {
                startCalibrationWithSensor()
                changeProgressIndicatorEnabled(true)
                changeStartButtonEnabled(false)
            }
            CalibrationUiState.State.CALIBRATED -> {
                endSensorListening()
                changeProgressIndicatorEnabled(false)
                changeStartButtonEnabled(true)
                changeLayoutByIsCalibrated(true)
            }
        }
    }

    private fun changeLayoutByIsCalibrated(isCalibrated: Boolean) {
        val headerTextResId =
            if (isCalibrated) R.string.fragment_calibration_header_text_calibrated
            else R.string.fragment_calibration_header_text_idle
        val descriptionTextResId =
            if (isCalibrated) R.string.fragment_calibration_description_text_calibrated
            else R.string.fragment_calibration_description_text_idle
        val startButtonCaptionResId =
            if (isCalibrated) R.string.fragment_calibration_button_start_caption_calibrated
            else R.string.fragment_calibration_button_start_caption_idle

        mBinding.fragmentCalibrationHeader.setText(headerTextResId)
        mBinding.fragmentCalibrationDescription.setText(descriptionTextResId)
        mBinding.fragmentCalibrationPhoneImage.setImageResource(R.drawable.ic_moving_phone)
        mBinding.fragmentCalibrationButtonStart.setText(startButtonCaptionResId)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d(TAG, "onSensorChanged(): event.x = ${event?.values?.get(0)};" +
                " event.y = ${event?.values?.get(1)}; " +
                "event.z = ${event?.values?.get(2)}")

        mModel.setLastOffsets(event!!.values!![0], event.values!![1], event.values!![2])
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }
}