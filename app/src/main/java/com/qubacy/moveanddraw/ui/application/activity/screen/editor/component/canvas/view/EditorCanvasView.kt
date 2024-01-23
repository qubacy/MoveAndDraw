package com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.view

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.AbsSavedState
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.view.CanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.renderer.EditorCanvasRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.abs

class EditorCanvasView(
    context: Context, attributeSet: AttributeSet
) : CanvasView(
    context, attributeSet
) {
    companion object {
        const val SKETCH_FACE_DOT_ARRAY_KEY = "sketchFaceDotArray"
        const val SUPER_STATE_KEY = "superState"

        private const val MIN_MOVE_DETECTION_DISTANCE_IN_PX = 10
    }

    override val mRenderer: EditorCanvasRenderer = EditorCanvasRenderer()

    private var mIsInEditorMode: Boolean = false
    private var mIsLongAction = false

    fun enableEditorMode(isEnabled: Boolean) {
        mIsInEditorMode = isEnabled

        val editorRendererMode =
            if (isEnabled) EditorCanvasRenderer.EditorRendererMode.CREATING_FACE
            else EditorCanvasRenderer.EditorRendererMode.VIEWING

        mRenderer.setMode(editorRendererMode)
        requestRender()
    }

    suspend fun removeLastSketchVertex() {
        mRenderer.removeLastSketchFaceVertex()
        requestRender()
    }

    override fun processMoveTouchEventAction(e: MotionEvent): Boolean {
        super.processMoveTouchEventAction(e)

        Log.d(TAG, "processMoveTouchEventAction(): mPrevDX = $mPrevDX; mPrevDY = $mPrevDY;")

        if (abs(mPrevDX) >= MIN_MOVE_DETECTION_DISTANCE_IN_PX
         || abs(mPrevDY) >= MIN_MOVE_DETECTION_DISTANCE_IN_PX
        ) {
            mIsLongAction = true
        }

        return true
    }

    override fun processOtherTouchEventAction(e: MotionEvent): Boolean {
        Log.d(TAG, "processOtherTouchEventAction(): mIsMoving = $mIsLongAction;")

        when (e.action) {
            MotionEvent.ACTION_UP -> {
                if (mIsLongAction) {
                    mIsLongAction = false

                } else {
                    mLifecycleScope?.launch(Dispatchers.IO) {
                        mRenderer.handleClick(e.x, e.y)
                        requestRender()
                    }
                }
            }
        }

        return true
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

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        mIsLongAction = true

        return true
    }
}