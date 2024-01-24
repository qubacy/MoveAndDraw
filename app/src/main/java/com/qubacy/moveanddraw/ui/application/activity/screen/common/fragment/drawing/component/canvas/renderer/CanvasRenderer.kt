package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.annotation.FloatRange
import com.qubacy.moveanddraw._common.util.struct.takequeue.mutable.MutableTakeQueue
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.GLContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model.GLDrawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer.command._common.RenderCommand
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

open class CanvasRenderer(

) : GLSurfaceView.Renderer {
    companion object {
        const val TAG = "CANVAS_RENDERER"

        private val CENTER_POSITION = floatArrayOf(0f, 0f, 0f)
        const val DEFAULT_SPHERE_RADIUS = 2f

        private const val MIN_SCALE_FACTOR = 0.25f
        private const val MAX_SCALE_FACTOR = 100f

        const val DEFAULT_CAMERA_NEAR = 0.01f
        private const val CAMERA_FOV = 60f
    }

    protected val mVPMatrix = FloatArray(16)
    protected val mProjectionMatrix = FloatArray(16)
    protected val mViewMatrix = FloatArray(16)

    protected var mSphereRadius = DEFAULT_SPHERE_RADIUS
    protected var mCameraRadius = mSphereRadius
    protected var mCameraNear = DEFAULT_CAMERA_NEAR

    @Volatile
    protected var mCameraCenterLocation = floatArrayOf(0f, 0f, 0f)
    @Volatile
    protected var mViewCenterLocation = floatArrayOf(0f, 0f, 0f)
    @Volatile
    private var mCameraLocation = floatArrayOf(mCameraRadius, 0f, mCameraCenterLocation[2])
    @Volatile
    private var mCameraMadeWayHorizontal = 0f
    @Volatile
    private var mCameraMadeWayVertical = 0f
    @Volatile
    private var mViewportRatio = 1f
    @Volatile
    private var mCurScaleFactor = 1f

    protected var mFigure: GLDrawing? = null
    protected val mFigureMutex = Mutex(false)

    @Volatile
    private var mIsCameraLocationInitialized = false
    @Volatile
    private var mBackgroundColor: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)
    @Volatile
    private var mDefaultModelColor: FloatArray = floatArrayOf(1f, 1f, 1f, 1f)

    protected var mDeviceWidth: Int = 0
    protected var mDeviceHeight: Int = 0

    private var mRenderCommandQueue = MutableTakeQueue<RenderCommand>()

    protected fun getFigureCenterPoint(figure: GLDrawing): FloatArray {
        var minX = figure.vertexArray[0]
        var maxX = figure.vertexArray[0]

        var minY = figure.vertexArray[1]
        var maxY = figure.vertexArray[1]

        var minZ = figure.vertexArray[2]
        var maxZ = figure.vertexArray[2]

        for (i in 0 until figure.vertexArray.size - 2 step (3)) {
            if (figure.vertexArray[i + 0] < minX) minX = figure.vertexArray[i + 0]
            if (figure.vertexArray[i + 0] > maxX) maxX = figure.vertexArray[i + 0]

            if (figure.vertexArray[i + 1] < minY) minY = figure.vertexArray[i + 1]
            if (figure.vertexArray[i + 1] > maxY) maxY = figure.vertexArray[i + 1]

            if (figure.vertexArray[i + 2] < minZ) minZ = figure.vertexArray[i + 2]
            if (figure.vertexArray[i + 2] > maxZ) maxZ = figure.vertexArray[i + 2]
        }

        return floatArrayOf(
            (minX + maxX) / 2,
            (minY + maxY) / 2,
            (minZ + maxZ) / 2
        )
    }

     protected suspend fun addRenderCommand(renderCommand: RenderCommand) {
        mRenderCommandQueue.put(renderCommand)
    }

    fun setModelColor(
        @FloatRange(0.0, 1.0) r: Float,
        @FloatRange(0.0, 1.0) g: Float,
        @FloatRange(0.0, 1.0) b: Float,
        @FloatRange(0.0, 1.0) a: Float
    ) {
        mDefaultModelColor = floatArrayOf(r, g, b, a)

        mFigure?.apply {
            setColor(mDefaultModelColor)
        }
    }

    fun setBackgroundColor(
        @FloatRange(0.0, 1.0) r: Float,
        @FloatRange(0.0, 1.0) g: Float,
        @FloatRange(0.0, 1.0) b: Float,
        @FloatRange(0.0, 1.0) a: Float
    ) {
        mBackgroundColor = floatArrayOf(r, g, b, a)
    }

    fun setFigureDrawingMode(drawingMode: GLContext.DrawingMode) {
        mFigure?.setDrawingMode(drawingMode)
    }

    suspend fun setFigure(figure: GLDrawing) = mFigureMutex.withLock {
        mFigure = figure.apply {
            setColor(mDefaultModelColor)
        }

        mSphereRadius = mFigure!!.vertexArray.map { abs(it) }.max() + DEFAULT_SPHERE_RADIUS
        mCameraRadius = mSphereRadius

        mViewCenterLocation = getFigureCenterPoint(figure)
        mCameraCenterLocation = floatArrayOf(0f, 0f, mViewCenterLocation[2])

        mCameraMadeWayHorizontal = 0f
        mCameraMadeWayVertical = 0f
        mCurScaleFactor = 1f
        mIsCameraLocationInitialized = false

        setDefaultCameraLocation()
        setPerspective()

        mIsCameraLocationInitialized = true
    }

    private fun getTranslatedCameraLocation(dx: Float, dy: Float): FloatArray {
        val signedDX = dx * -1
        val signedDY = dy * 1

        var newX = mCameraLocation[0]
        var newY = mCameraLocation[1]
        var newZ = mCameraLocation[2]

        if (abs(signedDX) >= abs(signedDY)) {
            val cameraWayLength = (2 * PI * mCameraRadius).toFloat()
            val cameraMadeWay = (signedDX + mCameraMadeWayHorizontal) % cameraWayLength
            val cameraMadeWayNormalized =
                if (cameraMadeWay < 0) cameraMadeWay + cameraWayLength
                else cameraMadeWay

            val madeWayAngle = cameraMadeWayNormalized / mCameraRadius

            newX = mCameraCenterLocation[0] + mCameraRadius * cos(madeWayAngle)
            newY = mCameraCenterLocation[1] + mCameraRadius * sin(madeWayAngle)

            mCameraMadeWayHorizontal = cameraMadeWayNormalized

        } else {
            val cameraWayLength = (0.8 * PI * mSphereRadius / 2).toFloat()
            val cameraMadeWayNormalized = signedDY + mCameraMadeWayVertical

            if (abs(cameraMadeWayNormalized) >= cameraWayLength) return mCameraLocation

            val madeWayAngleVertical = cameraMadeWayNormalized / mSphereRadius

            newZ = CENTER_POSITION[2] + mSphereRadius * sin(madeWayAngleVertical)
            val newCameraRadius = sqrt(mSphereRadius * mSphereRadius - newZ * newZ)

            mCameraMadeWayHorizontal *= (newCameraRadius / mCameraRadius)
            mCameraRadius = newCameraRadius

            val madeWayAngleHorizontal = mCameraMadeWayHorizontal / mCameraRadius

            newX = mCameraCenterLocation[0] + mCameraRadius * cos(madeWayAngleHorizontal)
            newY = mCameraCenterLocation[1] + mCameraRadius * sin(madeWayAngleHorizontal)

            mCameraMadeWayVertical = cameraMadeWayNormalized
        }

        return floatArrayOf(newX, newY, newZ)
    }

    open fun handleRotation(dx: Float, dy: Float) {
        mCameraLocation = getTranslatedCameraLocation(dx, dy)
    }

    open fun handleScale(scaleFactor: Float) {
        val newScaleFactor = mCurScaleFactor * scaleFactor

        if (newScaleFactor !in MIN_SCALE_FACTOR..MAX_SCALE_FACTOR) return

        mCurScaleFactor = newScaleFactor

        setPerspective()
    }

    protected fun setPerspective() {
        Matrix.perspectiveM(
            mProjectionMatrix, 0,
            CAMERA_FOV, mViewportRatio,
            mCameraNear,
            mSphereRadius * 2
        )
    }

    private fun setDefaultCameraLocation() {
        if (mIsCameraLocationInitialized) return

        val initCameraVerticalMadeWay = PI * mSphereRadius / 4f

        mCameraLocation = floatArrayOf(mCameraRadius, 0f, mCameraCenterLocation[2])
        mCameraLocation = getTranslatedCameraLocation(0f, initCameraVerticalMadeWay.toFloat())
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(mBackgroundColor[0], mBackgroundColor[1], mBackgroundColor[2], mBackgroundColor[3])

        mFigure?.init()

        setDefaultCameraLocation()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mDeviceWidth = width
        mDeviceHeight = height

        GLES20.glViewport(0, 0, width, height)

        mViewportRatio = width.toFloat() / height.toFloat()

        setPerspective()
    }

    override fun onDrawFrame(gl: GL10?): Unit = runBlocking {
        executePendingRenderCommands()

        mFigureMutex.withLock {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

            Matrix.setLookAtM(
                mViewMatrix, 0,
                mCameraLocation[0] * (1 / mCurScaleFactor),
                mCameraLocation[1] * (1 / mCurScaleFactor),
                mCameraLocation[2] * (1 / mCurScaleFactor),
                mViewCenterLocation[0], mViewCenterLocation[1], mViewCenterLocation[2],
                0f, 0f, 1.0f
            )
            Matrix.multiplyMM(
                mVPMatrix, 0,
                mProjectionMatrix, 0,
                mViewMatrix, 0
            )

            drawFigure()
        }
    }

    private fun drawFigure() {
        if (mFigure?.isInitialized == false) mFigure!!.init()

        mFigure?.draw(mVPMatrix)
    }

    private suspend fun executePendingRenderCommands() {
        while (true) {
            val command = mRenderCommandQueue.take() ?: break

            executeRenderCommand(command)
        }
    }

    private fun executeRenderCommand(renderCommand: RenderCommand) {
        when (renderCommand::class) {
            else -> processRenderCommand(renderCommand)
        }
    }

    protected open fun processRenderCommand(command: RenderCommand) {

    }
}