package com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.renderer.initializer

import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer.initializer.RendererStepInitializer

class EditorRendererStepInitializer(

) : RendererStepInitializer() {
    enum class EditorStep : Step {
        SKETCH();
    }

    @Volatile
    private var mSketchDotList: List<Pair<Float, Float>>? = null
    val sketch get() = mSketchDotList

    fun postponeSketchDotList(sketchDotList: List<Pair<Float, Float>>) {
        mSketchDotList = sketch
    }

    override fun getAdditionalNextStep(): Step {
        return when (mCurrentStep) {
            StandardStep.CAMERA -> EditorStep.SKETCH
            EditorStep.SKETCH -> StandardStep.FINAL
            else -> throw IllegalStateException()
        }
    }

    override fun reset() {
        super.reset()

        mSketchDotList = null
    }
}