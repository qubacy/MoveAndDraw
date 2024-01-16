package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.qubacy.moveanddraw._common.error.ErrorEnum
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model._common.state._common.UiState
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment._common.model.business.BusinessViewModel
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.accelerometer.model._common.AccelerometerStateHolder

interface AccelerometerFragment<
    UiStateType : UiState,
    ViewModelType : BusinessViewModel<UiStateType>
> : SensorEventListener {
    companion object {
        const val TAG = "ACCELER_FRAGMENT"
    }

    fun getAccelerometerStateHolder(): AccelerometerStateHolder
    fun getAccelerometerModel(): ViewModelType
    fun getFragmentContext(): Context

    fun startSensorListening() {
        val sensorManager = ContextCompat.getSystemService(
            getFragmentContext(),
            SensorManager::class.java
        ) as SensorManager
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        if (sensor == null)
            return getAccelerometerModel().retrieveError(ErrorEnum.ACCELEROMETER_UNAVAILABLE.id)

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun endSensorListening() {
        val sensorManager = ContextCompat.getSystemService(
            getFragmentContext(),
            SensorManager::class.java
        ) as SensorManager

        sensorManager.unregisterListener(this)
    }

    fun checkSensorEventValidity(event: SensorEvent?): Boolean {
        return event != null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!checkSensorEventValidity(event)) return

        Log.d(
            TAG, "onSensorChanged(): event.x = ${event?.values?.get(0)};" +
                " event.y = ${event?.values?.get(1)}; " +
                "event.z = ${event?.values?.get(2)}")

        getAccelerometerStateHolder()
            .applyAccelerations(event!!.values!![0], event.values!![1], event.values!![2])
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }
}