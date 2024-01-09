package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.qubacy.moveanddraw.R
import com.qubacy.moveanddraw.domain._common.model.drawing.Drawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.mapper.DrawingGLDrawingMapper
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.mapper.DrawingGLDrawingMapperImpl
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer.CanvasRenderer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

class CanvasView(
    context: Context,
    attrs: AttributeSet
) : GLSurfaceView(context, attrs),
    ScaleGestureDetector.OnScaleGestureListener {
    companion object {
        const val TAG = "CANVAS_VIEW"

        private const val TOUCH_SCALE_FACTOR: Float = 180.0f / 12800f
        private const val AFTER_SCALE_DELAY = 300L
    }

    private val mRenderer: CanvasRenderer
    private val mScaleGestureDetector: ScaleGestureDetector

    private var mLastScaleEventTimestamp = 0L

    private var previousX: Float = 0f
    private var previousY: Float = 0f

    private var mDrawingMapper: DrawingGLDrawingMapper? = null

    init {
        setEGLContextClientVersion(2)

        mRenderer = CanvasRenderer()
        mScaleGestureDetector = ScaleGestureDetector(context, this)

        setRenderer(mRenderer)
        initCustomAttrs(attrs)

        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    @SuppressLint("Recycle")
    private fun initCustomAttrs(attrs: AttributeSet) {
        val attrsTypedArray = context
            .obtainStyledAttributes(attrs, intArrayOf(R.attr.canvasBackgroundColor))
        val canvasBackgroundColor = attrsTypedArray.getColor(0, 0)

        val r = Color.red(canvasBackgroundColor) / 255f
        val g = Color.green(canvasBackgroundColor) / 255f
        val b = Color.blue(canvasBackgroundColor) / 255f
        val a = Color.alpha(canvasBackgroundColor) / 255f

        setCanvasBackgroundColor(r, g, b, a)
    }

    private fun setCanvasBackgroundColor(r: Float, g: Float, b: Float, a: Float) {
        mRenderer.setBackgroundColor(r, g, b, a)
        requestRender()
    }

    fun setDrawingMapper(drawingGLDrawingMapper: DrawingGLDrawingMapper) {
        mDrawingMapper = drawingGLDrawingMapper
    }

    suspend fun setFigure(figure: Drawing) { // todo: is it throwable?
        if (mDrawingMapper == null) return

        val glDrawing = mDrawingMapper!!.map(figure)

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

@Module
@InstallIn(ActivityRetainedComponent::class)
object CanvasViewModel {
    @Provides
    fun provideDrawingMapper(): DrawingGLDrawingMapper {
        return DrawingGLDrawingMapperImpl()
    }
}