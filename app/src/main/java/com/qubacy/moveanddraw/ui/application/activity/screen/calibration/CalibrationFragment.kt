package com.qubacy.moveanddraw.ui.application.activity.screen.calibration

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw.databinding.FragmentCalibrationBinding
import com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model.CalibrationViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model.CalibrationViewModelFactoryQualifier
import com.qubacy.moveanddraw.ui.application.activity.screen.calibration.model.state.CalibrationUiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.transition.DefaultSharedAxisTransitionGenerator
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.AccelerometerFragment
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.model._common.AccelerometerStateHolder
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IllegalStateException
import javax.inject.Inject

@AndroidEntryPoint
class CalibrationFragment(

) : BaseFragment<CalibrationUiState, CalibrationViewModel>(),
    AccelerometerFragment<CalibrationUiState, CalibrationViewModel>
{
    companion object {
        const val TAG = "CALIBR_FRAGMENT"
    }

    @Inject
    @CalibrationViewModelFactoryQualifier
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val mModel: CalibrationViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private lateinit var mBinding: FragmentCalibrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = DefaultSharedAxisTransitionGenerator.generate(
            requireContext(),
            MaterialSharedAxis.Z,
            true
        )
        returnTransition = DefaultSharedAxisTransitionGenerator.generate(
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
        if (mModel.uiState.value == null) return startCalibration()

        when (mModel.uiState.value!!.state) {
            CalibrationUiState.State.IDLE -> { startCalibration() }
            CalibrationUiState.State.CALIBRATED -> { goToEditor() }
            else -> throw IllegalStateException()
        }
    }

    private fun startCalibration() {
        mModel.startCalibration()
    }

    private fun goToEditor() {
        val offsets = floatArrayOf(mModel.xLastOffset, mModel.yLastOffset, mModel.zLastOffset)
        val action = CalibrationFragmentDirections.actionCalibrationFragmentToEditorFragment(offsets)

        findNavController().navigate(action)
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
                startSensorListening()
                changeProgressIndicatorEnabled(true)
                changeStartButtonEnabled(false)
            }
            CalibrationUiState.State.CALIBRATED -> {
                endSensorListening()
                changeProgressIndicatorEnabled(false)
                changeStartButtonEnabled(true)
            }
        }

        changeLayoutByState(uiState.state)
    }

    private fun changeLayoutByState(state: CalibrationUiState.State) {
        val headerTextResId = when (state) {
            CalibrationUiState.State.CALIBRATED ->
                R.string.fragment_calibration_header_text_calibrated
            else -> R.string.fragment_calibration_header_text_idle
        }
        val descriptionTextResId = when (state) {
            CalibrationUiState.State.IDLE ->
                R.string.fragment_calibration_description_text_idle
            CalibrationUiState.State.CALIBRATING ->
                R.string.fragment_calibration_description_text_calibrating
            CalibrationUiState.State.CALIBRATED ->
                R.string.fragment_calibration_description_text_calibrated
        }
        val imageResourceId = when (state) {
            CalibrationUiState.State.CALIBRATED -> R.drawable.ic_moving_phone
            else -> R.drawable.ic_phone
        }
        val startButtonCaptionResId = when (state) {
            CalibrationUiState.State.CALIBRATED ->
                R.string.fragment_calibration_button_start_caption_calibrated
            else -> R.string.fragment_calibration_button_start_caption_idle
        }

        mBinding.fragmentCalibrationHeader.setText(headerTextResId)
        mBinding.fragmentCalibrationDescription.setText(descriptionTextResId)
        mBinding.fragmentCalibrationPhoneImage.setImageResource(imageResourceId)
        mBinding.fragmentCalibrationButtonStart.setText(startButtonCaptionResId)
    }

    override fun getAccelerometerStateHolder(): AccelerometerStateHolder {
        return mModel
    }

    override fun getAccelerometerModel(): CalibrationViewModel {
        return mModel
    }

    override fun getFragmentContext(): Context {
        return requireContext()
    }
}