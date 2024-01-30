package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.camera.mutable

import android.util.Log
import com.qubacy.moveanddraw.domain._common.model.drawing._common.DrawingContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.camera._common.CameraContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.camera._common.CameraData

class MutableCameraData(
    position: FloatArray,
    fov: Float,
    scaleFactor: Float,
    madeWayHorizontal: Float,
    madeWayVertical: Float
) : CameraData(position, fov, scaleFactor, madeWayHorizontal, madeWayVertical) {
    companion object {
        const val TAG = "MUTABLE_CAMERA_DATA"
    }

    fun setPosition(position: FloatArray) {
        if (position.size != DrawingContext.COORDS_PER_VERTEX)
            throw IllegalArgumentException()

        mPosition = position
    }

    fun setFOV(fov: Float) {
        if (fov !in CameraContext.MIN_FOV..CameraContext.MAX_FOV)
            throw IllegalArgumentException()

        mFOV = fov
    }

    fun setScaleFactor(scaleFactor: Float) {
        if (scaleFactor < CameraContext.MIN_SCALE)
            throw IllegalArgumentException()

        mScaleFactor = scaleFactor
    }

    fun setMadeWayHorizontal(madeWayHorizontal: Float) {
        mMadeWayHorizontal = madeWayHorizontal
    }

    fun setMadeWayVertical(madeWayVertical: Float) {
        mMadeWayVertical = madeWayVertical
    }

    fun setData(cameraData: CameraData) {
        Log.d(TAG, "setData(): cameraData.pos = ${cameraData.position.joinToString()}")

        mPosition = cameraData.position
        mFOV = cameraData.fov
        mScaleFactor = cameraData.scaleFactor
        mMadeWayHorizontal = cameraData.madeWayHorizontal
        mMadeWayVertical = cameraData.madeWayVertical
    }
}