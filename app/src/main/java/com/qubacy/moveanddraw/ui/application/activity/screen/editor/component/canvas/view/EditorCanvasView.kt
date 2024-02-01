package com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.qubacy.moveanddraw.domain._common.model.drawing._common.Drawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.Dot2D
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.GLContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.view.CanvasView
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas._common.EditorCanvasContext
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.data.face.FaceSketch
import com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.renderer.EditorCanvasRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

class EditorCanvasView(
    context: Context, attributeSet: AttributeSet
) : CanvasView(
    context, attributeSet
) {
    companion object {
        const val TAG = "EDITOR_CANVAS_VIEW"

        private const val MIN_MOVE_DETECTION_DISTANCE_IN_PX = 10
    }

    override val mRenderer: EditorCanvasRenderer = EditorCanvasRenderer()

    private var mIsLongAction = false

    private var mIsSavingSketch: Boolean = false

    fun getFaceSketchDotBuffer(): List<Dot2D> {
        val copyBuffer = mutableListOf<Dot2D>().apply {
            for (dot in mRenderer.faceSketchDotBuffer) add(dot.copy())
        }

        return copyBuffer
    }

    fun setFaceSketchDotBuffer(
        faceSketchDotBuffer: List<Dot2D>
    ) = mLifecycleScope?.launch (Dispatchers.IO) {
        mRenderer.setFaceSketchDotBuffer(faceSketchDotBuffer)
        requestRender()
    }

    fun getEditorMode(): EditorCanvasContext.Mode {
        return mRenderer.editorRendererMode
    }

    fun setEditorMode(
        editorMode: EditorCanvasContext.Mode,
        isInitializing: Boolean = false
    ) = mLifecycleScope?.launch (Dispatchers.IO) {
        mRenderer.setEditorRendererMode(editorMode, isInitializing)
        requestRender()
    }

    suspend fun removeLastSketchVertex() {
        mRenderer.removeLastSketchFaceVertex()
        requestRender()
    }

    suspend fun saveAndGetFaceSketch(): FaceSketch? {
        val faceSketch = mRenderer.saveAndGetFaceSketch()

        if (faceSketch != null) mIsSavingSketch = true

        return faceSketch
    }

    /**
     It's supposed that the Sketch Face saving has to be ended by calling setFigure(..) method
     with a renewed Drawing as an argument;
     */
    override suspend fun setFigure(figure: Drawing, drawingMode: GLContext.DrawingMode?) {
        if (mIsSavingSketch) {
            mIsSavingSketch = false

            return
        }

        super.setFigure(figure, drawingMode)
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

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        mIsLongAction = true

        return true
    }
}