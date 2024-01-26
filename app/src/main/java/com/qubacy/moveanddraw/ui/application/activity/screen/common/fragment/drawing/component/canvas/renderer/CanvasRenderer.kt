package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import androidx.annotation.FloatRange
import com.qubacy.moveanddraw._common.util.struct.takequeue.mutable.MutableTakeQueue
import com.qubacy.moveanddraw.domain._common.model.drawing._common.DrawingContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.GLContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model.GLDrawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer.command._common.RenderCommand
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.util.GL2Util
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

        val CENTER_POSITION = floatArrayOf(0f, 0f, 0f)
        const val DEFAULT_SPHERE_RADIUS = 1f
        const val DEFAULT_SPHERE_RADIUS_COEF = 3f

        const val MIN_SCALE_FACTOR = 0.25f
        const val MAX_SCALE_FACTOR = 100f

        const val DEFAULT_CAMERA_NEAR = 0.01f
        const val CAMERA_NEAR_DRAWING_GAP = 0.001f
        private const val CAMERA_FOV = 60f
    }

    protected val mProjectionMatrix = FloatArray(16)
    protected val mViewMatrix = FloatArray(16)
    protected val mVPMatrix = FloatArray(16)

    protected var mSphereRadius: Float = DEFAULT_SPHERE_RADIUS
    protected var mCameraRadius = mSphereRadius
    protected var mCameraNear = DEFAULT_CAMERA_NEAR

    @Volatile
    protected var mCameraCenterLocation = floatArrayOf(0f, 0f, 0f)
    @Volatile
    protected var mViewCenterLocation = floatArrayOf(0f, 0f, 0f)
    @Volatile
    protected var mCameraLocation = floatArrayOf(mCameraRadius, 0f, mCameraCenterLocation[2])
    @Volatile
    protected var mCameraMadeWayHorizontal = 0f
    @Volatile
    protected var mCameraMadeWayVertical = 0f
    @Volatile
    protected var mViewportRatio = 1f
    @Volatile
    protected var mCurScaleFactor = 1f
    @Volatile
    protected var mFigureVolumeCoef = 1f

    protected var mFigure: GLDrawing? = null
    protected val mFigureMutex = Mutex(false)

    @Volatile
    private var mBackgroundColor: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)
    @Volatile
    private var mDefaultModelColor: FloatArray = floatArrayOf(1f, 1f, 1f, 1f)

    protected var mDeviceWidth: Int = 0
    protected var mDeviceHeight: Int = 0

    private var mRenderCommandQueue = MutableTakeQueue<RenderCommand>()

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
        setFigureData(figure)
    }

    protected fun setFigureData(figure: GLDrawing) {
        prepareForFigure(figure)

        mCameraMadeWayHorizontal = 0f
        mCameraMadeWayVertical = 0f
        mCurScaleFactor = 1f

        setDefaultCameraLocation()
        setPerspective()
    }

    /**
     * Note: this method should be called DURING mFigureMutex LOCKING!
     */
    protected fun prepareForFigure(figure: GLDrawing) {
        mFigure = figure.apply {
            setColor(mDefaultModelColor)
        }

        val usedVertexArray = if (mFigure!!.vertexDrawingOrder != null) {
            val vertexIndices = mFigure!!.vertexDrawingOrder!!.toSet().toShortArray()

            GL2Util.filterVertexArrayWithIndices(mFigure!!.vertexArray, vertexIndices)

        } else {
            mFigure!!.vertexArray
        }

        mViewCenterLocation = GL2Util.getVertexCenterPoint(usedVertexArray)

        val sphereRadiusFromDistance =
            GL2Util.getMaxDistanceFromDot(usedVertexArray, mViewCenterLocation)
        val minSphereRadius = DEFAULT_CAMERA_NEAR + CAMERA_NEAR_DRAWING_GAP
        val sphereRadius =
            if (sphereRadiusFromDistance > minSphereRadius) sphereRadiusFromDistance
            else minSphereRadius

        mSphereRadius = sphereRadius * DEFAULT_SPHERE_RADIUS_COEF
        mCameraRadius = mSphereRadius

        mCameraCenterLocation = mViewCenterLocation
        mFigureVolumeCoef = getFigureVolumeCoefBySphereRadiusAndMaxDistance(
            mSphereRadius, sphereRadiusFromDistance)

        Log.d(TAG, "prepareForFigure(): sphereRadiusFromDistance = $sphereRadiusFromDistance;")
        Log.d(TAG, "prepareForFigure(): mSphereRadius = $mSphereRadius; mFigureVolumeCoef = $mFigureVolumeCoef;")
        Log.d(TAG, "prepareForFigure(): mViewCenterLocation = ${mViewCenterLocation.joinToString()};")
    }

    protected fun getFigureVolumeCoefBySphereRadiusAndMaxDistance(
        sphereRadius: Float,
        maxDistance: Float
    ): Float {
        return if (sphereRadius < 1f)
            maxDistance  / ((-10 * maxDistance + 10) * sphereRadius)
        else
            maxDistance / sphereRadius
    }

    protected fun getVerticalCameraWayLength(): Float {
        return (0.8 * PI * mSphereRadius / 2).toFloat()
    }

    protected fun getHorizontalCameraWayLength(): Float {
        return (2 * PI * mCameraRadius).toFloat()
    }

    private fun getTranslatedCameraLocation(dx: Float, dy: Float): FloatArray {
        val preparedDX = dx * -1 * mFigureVolumeCoef * (1 / mCurScaleFactor)
        val preparedDY = dy * 1 * mFigureVolumeCoef * (1 / mCurScaleFactor)

        var newX = mCameraLocation[0]
        var newY = mCameraLocation[1]
        var newZ = mCameraLocation[2]

        if (abs(preparedDX) >= abs(preparedDY)) {
            val cameraWayLength = getHorizontalCameraWayLength()
            val cameraMadeWay = (preparedDX + mCameraMadeWayHorizontal) % cameraWayLength
            val cameraMadeWayNormalized =
                if (cameraMadeWay < 0) cameraMadeWay + cameraWayLength
                else cameraMadeWay

            val madeWayAngle = cameraMadeWayNormalized / mCameraRadius

            newX = mCameraCenterLocation[0] + mCameraRadius * cos(madeWayAngle)
            newY = mCameraCenterLocation[1] + mCameraRadius * sin(madeWayAngle)

            mCameraMadeWayHorizontal = cameraMadeWayNormalized

        } else {
            val cameraWayLength = getVerticalCameraWayLength()
            val cameraMadeWayNormalized = preparedDY + mCameraMadeWayVertical

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

        Log.d(TAG, "handleScale(): newScaleFactor = $newScaleFactor")

        mCurScaleFactor = newScaleFactor

        setPerspective()
    }

    protected fun setPerspective() {
        val scaledFOV = CAMERA_FOV / (mCurScaleFactor)

        Matrix.perspectiveM(
            mProjectionMatrix, 0,
            scaledFOV,
            mViewportRatio,
            mCameraNear,
            mSphereRadius * 2
        )
    }

    /**
     * Note: an angle between X & Camera trajectory is 45 deg;
     */
    protected fun setDefaultCameraLocation() {
        //val initCameraVerticalMadeWay = PI * mSphereRadius / 4f // todo: think about this;

        mCameraLocation = floatArrayOf(
            mViewCenterLocation[0] + mCameraRadius, mViewCenterLocation[1], mViewCenterLocation[2])
        //mCameraLocation = getTranslatedCameraLocation(0f, initCameraVerticalMadeWay.toFloat())

        Log.d(TAG, "setDefaultCameraLocation(): mCameraLocation = (${mCameraLocation.joinToString()});")
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
                mCameraLocation[0],
                mCameraLocation[1],
                mCameraLocation[2],
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