package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.annotation.ColorInt
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.GLContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.mapper.DrawingGLDrawingMapper
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer.CanvasRenderer
import kotlinx.coroutines.runBlocking

open class CanvasView(
    context: Context,
    attrs: AttributeSet
) : GLSurfaceView(context, attrs),
    ScaleGestureDetector.OnScaleGestureListener
{
    companion object {
        const val TAG = "CANVAS_VIEW"

        private const val TOUCH_SCALE_FACTOR: Float = 180.0f / 25600f
        private const val AFTER_SCALE_DELAY = 300L

        private const val DEFAULT_COLOR_INT = -1
    }

    protected open val mRenderer: CanvasRenderer = CanvasRenderer()
    protected val mScaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, this)

    protected var mLastScaleEventTimestamp = 0L

    protected var previousX: Float = 0f
    protected var previousY: Float = 0f

    protected var mDrawingMapper: DrawingGLDrawingMapper? = null

    protected var mCurrentDrawing: Drawing? = null

    protected var mCanvasBackgroundColor: FloatArray = floatArrayOf(0f, 0f, 0f, 1f)
    protected var mCanvasModelColor: FloatArray = floatArrayOf(1f, 1f, 1f, 1f)

    init {
        initCustomAttrs(attrs)
    }

    fun init() = runBlocking {
        setEGLContextClientVersion(2)
        setRenderer(mRenderer)

        setCanvasBackgroundColor(mCanvasBackgroundColor)
        setCanvasModelColor(mCanvasModelColor)

        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    @SuppressLint("Recycle", "ResourceType")
    private fun initCustomAttrs(attrs: AttributeSet) {
        val attrsTypedArray = context.obtainStyledAttributes(
            attrs,
            intArrayOf(R.attr.canvasBackgroundColor, R.attr.canvasModelColor)
        )

        attrsTypedArray.getColor(0, -1).apply {
            if (this == DEFAULT_COLOR_INT) return@apply

            mCanvasBackgroundColor = colorToRGBAFloatArray(this)
        }
        attrsTypedArray.getColor(1, -1).apply {
            if (this == DEFAULT_COLOR_INT) return@apply

            mCanvasModelColor = colorToRGBAFloatArray(this)
        }
    }

    private fun colorToRGBAFloatArray(@ColorInt color: Int): FloatArray {
        return floatArrayOf(
            Color.red(color) / 255f,
            Color.green(color) / 255f,
            Color.blue(color) / 255f,
            Color.alpha(color) / 255f
        )
    }

    private fun setCanvasBackgroundColor(rgba: FloatArray) {
        mRenderer.setBackgroundColor(rgba[0], rgba[1], rgba[2], rgba[3])
        requestRender()
    }

    suspend fun setCanvasModelColor(rgba: FloatArray) {
        mRenderer.setModelColor(rgba[0], rgba[1], rgba[2], rgba[3])
        requestRender()
    }

    suspend fun setCanvasModelColor(color: Int) {
        setCanvasModelColor(colorToRGBAFloatArray(color))
    }

    suspend fun setCanvasModelColor(argb: IntArray) {
        val argbFloatArray = argb.toMutableList().map { it / 255f } as MutableList<Float>

        argbFloatArray.removeAt(0).also {
            argbFloatArray.add(it)
        }

        setCanvasModelColor(argbFloatArray.toFloatArray())
    }

    fun setDrawingMapper(drawingGLDrawingMapper: DrawingGLDrawingMapper) {
        mDrawingMapper = drawingGLDrawingMapper
    }

    fun setFigureDrawingMode(drawingMode: GLContext.DrawingMode) {
        mRenderer.setFigureDrawingMode(drawingMode)
        requestRender()
    }

    suspend fun setFigure(
        figure: Drawing,
        drawingMode: GLContext.DrawingMode? = null
    ) {
        if (mDrawingMapper == null) return

        mCurrentDrawing = figure

        val glDrawing = mDrawingMapper!!.map(figure)

        if (drawingMode != null) glDrawing.setDrawingMode(drawingMode)

        mRenderer.setFigure(glDrawing)
        requestRender()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x: Float = e.x
        val y: Float = e.y

        if (e.pointerCount == 2) {
            mScaleGestureDetector.onTouchEvent(e)
            requestRender()

            return true

        } else if (e.pointerCount > 2)
            return false

        when (e.action) {
            MotionEvent.ACTION_MOVE -> {
                val curTime = System.currentTimeMillis()

                if (mLastScaleEventTimestamp + AFTER_SCALE_DELAY > curTime)
                    return false

                val dx: Float = x - previousX
                val dy: Float = y - previousY

                mRenderer.handleRotation(dx * TOUCH_SCALE_FACTOR, dy * TOUCH_SCALE_FACTOR)

                requestRender()
            }
        }

        previousX = x
        previousY = y

        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        mRenderer.handleScale(detector.scaleFactor)

        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        mLastScaleEventTimestamp = System.currentTimeMillis()
    }
}