package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.camera._common

import java.io.Serializable

open class CameraData(
    initPosition: FloatArray = floatArrayOf(0f, 0f, 0f),
    initFOV: Float = CameraContext.DEFAULT_CAMERA_FOV,
    initScaleFactor: Float = CameraContext.MIN_SCALE,
    initMadeWayHorizontal: Float = 0f,
    initMadeWayVertical: Float = 0f,
    initCameraNear: Float = CameraContext.DEFAULT_CAMERA_NEAR
) : Serializable {
    @Volatile
    protected var mPosition: FloatArray = initPosition
    @Volatile
    protected var mFOV: Float = initFOV
    @Volatile
    protected var mScaleFactor: Float = initScaleFactor
    @Volatile
    protected var mMadeWayHorizontal: Float = initMadeWayHorizontal
    @Volatile
    protected var mMadeWayVertical: Float = initMadeWayVertical
    @Volatile
    protected var mCameraNear: Float = initCameraNear

    val position get() = mPosition
    val fov get() = mFOV
    val scaleFactor get() = mScaleFactor
    val madeWayHorizontal get() = mMadeWayHorizontal
    val madeWayVertical get() = mMadeWayVertical
    val cameraNear get() = mCameraNear

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CameraData

        if (!position.contentEquals(other.position)) return false
        if (fov != other.fov) return false
        if (scaleFactor != other.scaleFactor) return false
        if (madeWayHorizontal != other.madeWayHorizontal) return false
        if (madeWayVertical != other.madeWayVertical) return false
        if (cameraNear != other.cameraNear) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.contentHashCode()

        result = 31 * result + fov.hashCode()
        result = 31 * result + scaleFactor.hashCode()
        result = 31 * result + madeWayHorizontal.hashCode()
        result = 31 * result + madeWayVertical.hashCode()
        result = 31 * result + cameraNear.hashCode()

        return result
    }

    fun copy(): CameraData {
        return CameraData(
            mPosition.clone(),
            mFOV,
            mScaleFactor,
            mMadeWayHorizontal,
            mMadeWayVertical,
            mCameraNear
        )
    }
}