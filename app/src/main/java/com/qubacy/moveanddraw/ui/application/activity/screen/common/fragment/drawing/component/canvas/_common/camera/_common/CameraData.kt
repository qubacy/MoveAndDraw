package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.camera._common

import java.io.Serializable

open class CameraData(
    position: FloatArray = floatArrayOf(0f, 0f, 0f),
    fov: Float = CameraContext.MIN_FOV,
    scaleFactor: Float = CameraContext.MIN_SCALE,
    madeWayHorizontal: Float = 0f,
    madeWayVertical: Float = 0f
) : Serializable {
    @Volatile
    protected var mPosition: FloatArray = position
    @Volatile
    protected var mFOV: Float = fov
    @Volatile
    protected var mScaleFactor: Float = scaleFactor
    @Volatile
    protected var mMadeWayHorizontal: Float = madeWayHorizontal
    @Volatile
    protected var mMadeWayVertical: Float = madeWayVertical

    val position get() = mPosition
    val fov get() = mFOV
    val scaleFactor get() = mScaleFactor
    val madeWayHorizontal get() = mMadeWayHorizontal
    val madeWayVertical get() = mMadeWayVertical

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CameraData

        if (!position.contentEquals(other.position)) return false
        if (fov != other.fov) return false
        if (scaleFactor != other.scaleFactor) return false
        if (madeWayHorizontal != other.madeWayHorizontal) return false
        if (madeWayVertical != other.madeWayVertical) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.contentHashCode()

        result = 31 * result + fov.hashCode()
        result = 31 * result + scaleFactor.hashCode()
        result = 31 * result + madeWayHorizontal.hashCode()
        result = 31 * result + madeWayVertical.hashCode()

        return result
    }

    fun copy(): CameraData {
        return CameraData(
            mPosition.clone(),
            mFOV,
            mScaleFactor,
            mMadeWayHorizontal,
            mMadeWayVertical
        )
    }
}