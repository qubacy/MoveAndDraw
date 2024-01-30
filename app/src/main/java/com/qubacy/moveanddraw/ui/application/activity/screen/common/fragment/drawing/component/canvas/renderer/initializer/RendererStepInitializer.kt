package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer.initializer

import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.camera._common.CameraData
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model.GLDrawing

open class RendererStepInitializer(

) {
    interface Step { }

    enum class StandardStep : Step {
        FIGURE(), CAMERA(), FINAL();
    }

    @Volatile
    protected var mCurrentStep: Step = StandardStep.FIGURE
    val currentStep get() = mCurrentStep

    @Volatile
    protected var mFigure: GLDrawing? = null
    val figure get() = mFigure
    @Volatile
    protected var mCamera: CameraData? = null
    val camera get() = mCamera

    fun postponeFigure(figure: GLDrawing) {
        mFigure = figure
    }

    fun postponeCamera(camera: CameraData) {
        mCamera = camera
    }

    protected open fun getAdditionalNextStep(): Step {
        return when (mCurrentStep) {
            StandardStep.CAMERA -> StandardStep.FINAL
            else -> throw IllegalStateException()
        }
    }

    protected open fun getNextStep(): Step {
        return when (mCurrentStep) {
            StandardStep.FIGURE -> StandardStep.CAMERA
            else -> getAdditionalNextStep()
        }
    }

    fun nextStep() {
        val nextStep = getNextStep()

        mCurrentStep = nextStep
    }

    open fun reset() {
        mCurrentStep = StandardStep.FIGURE

        mFigure = null
        mCamera = null
    }
}