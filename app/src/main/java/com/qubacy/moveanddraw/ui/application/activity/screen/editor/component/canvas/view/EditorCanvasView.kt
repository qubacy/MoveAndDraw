package com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.view

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.AbsSavedState
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.view.CanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.renderer.EditorCanvasRenderer
import kotlinx.coroutines.runBlocking

class EditorCanvasView(
    context: Context, attributeSet: AttributeSet
) : CanvasView(
    context, attributeSet
) {
    companion object {
        const val SKETCH_FACE_DOT_ARRAY_KEY = "sketchFaceDotArray"
        const val SUPER_STATE_KEY = "superState"
    }

    override val mRenderer: EditorCanvasRenderer = EditorCanvasRenderer()

    private var mIsInEditorMode: Boolean = false
    private var mIsMoving = false

    fun enableEditorMode(isEnabled: Boolean) {
        mIsInEditorMode = isEnabled

        val editorRendererMode =
            if (isEnabled) EditorCanvasRenderer.EditorRendererMode.CREATING_FACE
            else EditorCanvasRenderer.EditorRendererMode.VIEWING

        mRenderer.setMode(editorRendererMode)
        requestRender()
    }

    override fun processMoveTouchEventAction(e: MotionEvent): Boolean {
        mIsMoving = true

        return super.processMoveTouchEventAction(e)
    }

    override fun processOtherTouchEventAction(e: MotionEvent) {
        when (e.action) {
            MotionEvent.ACTION_UP -> {
                if (mIsMoving) {
                    mIsMoving = false

                } else {
                    mRenderer.handleClick(e.x, e.y)
                    requestRender()
                }
            }
        }
    }

    // todo: rethink the following:
    override fun onRestoreInstanceState(state: Parcelable?) = runBlocking {
        val bundle = state as Bundle
        val superState = bundle.getParcelable<AbsSavedState>(SUPER_STATE_KEY)

        super.onRestoreInstanceState(superState)

        val sketchFaceDotArray = bundle.getFloatArray(SKETCH_FACE_DOT_ARRAY_KEY)!!

        mRenderer.setFaceSketchDotBuffer(sketchFaceDotArray)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        val sketchFaceDotArray = mRenderer.faceSketchDotBuffer
            .flatMap { listOf(it.first, it.second) }.toFloatArray()

        val bundle = Bundle().apply {
            putParcelable(SUPER_STATE_KEY, superState)
            putFloatArray(SKETCH_FACE_DOT_ARRAY_KEY, sketchFaceDotArray)
        }

        return bundle
    }
}