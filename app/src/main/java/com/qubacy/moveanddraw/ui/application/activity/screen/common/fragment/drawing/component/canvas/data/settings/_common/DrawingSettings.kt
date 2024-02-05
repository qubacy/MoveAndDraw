package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.settings._common

import android.os.Parcel
import android.os.Parcelable
import com.qubacy.moveanddraw.domain._common.model.drawing._common.DrawingContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.GLContext
import java.io.Serializable

open class DrawingSettings(
    initDrawingMode: GLContext.DrawingMode = GLContext.DrawingMode.FILLED,
    initModelColor: FloatArray = floatArrayOf(0f, 0f, 0f, 0f)
) : Parcelable {
    @Volatile
    protected var mDrawingMode: GLContext.DrawingMode = initDrawingMode
    @Volatile
    protected var mModelColor: FloatArray = initModelColor

    val drawingMode get() = mDrawingMode
    val modelColor get() = mModelColor

    constructor(
        parcel: Parcel
    ) : this() {
        val modelColorArray = FloatArray(DrawingContext.COLOR_COMPONENT_COUNT)

        parcel.readFloatArray(modelColorArray)

        mModelColor = modelColorArray
    }

    fun copy(): DrawingSettings {
        return DrawingSettings(
            mDrawingMode,
            mModelColor.clone()
        )
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloatArray(mModelColor)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DrawingSettings> {
        override fun createFromParcel(parcel: Parcel): DrawingSettings {
            return DrawingSettings(parcel)
        }

        override fun newArray(size: Int): Array<DrawingSettings?> {
            return arrayOfNulls(size)
        }
    }
}