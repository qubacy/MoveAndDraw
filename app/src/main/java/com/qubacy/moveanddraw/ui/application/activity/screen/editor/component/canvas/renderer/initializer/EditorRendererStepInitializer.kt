package com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.renderer.initializer

import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.Dot2D
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer.initializer.RendererStepInitializer
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas._common.EditorCanvasContext

class EditorRendererStepInitializer(

) : RendererStepInitializer() {
    enum class EditorStep : Step {
        EDITOR_MODE(), SKETCH();
    }

    @Volatile
    private var mEditorMode: EditorCanvasContext.Mode? = null
    val editorMode get() = mEditorMode

    @Volatile
    private var mSketchDotList: List<Dot2D>? = null
    val sketch get() = mSketchDotList

    fun postponeEditorMode(editorMode: EditorCanvasContext.Mode) {
        mEditorMode = editorMode
    }

    fun postponeSketchDotList(sketchDotList: List<Dot2D>) {
        mSketchDotList = sketchDotList
    }

    override fun getAdditionalNextStep(): Step {
        return when (mCurrentStep) {
            StandardStep.CAMERA -> EditorStep.EDITOR_MODE
            EditorStep.EDITOR_MODE -> EditorStep.SKETCH
            EditorStep.SKETCH -> StandardStep.FINAL
            else -> throw IllegalStateException()
        }
    }

    override fun reset() {
        super.reset()

        mEditorMode = null
        mSketchDotList = null
    }
}