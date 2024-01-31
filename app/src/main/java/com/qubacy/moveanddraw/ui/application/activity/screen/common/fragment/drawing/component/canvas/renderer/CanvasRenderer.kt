package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import androidx.annotation.FloatRange
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.GLContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.camera._common.CameraData
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.camera.mutable.MutableCameraData
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model.GLDrawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.settings._common.DrawingSettings
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.settings.mutable.MutableDrawingSettings
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer.initializer.RendererStepInitializer
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

    protected var mCameraData: MutableCameraData = MutableCameraData(
        floatArrayOf(mCameraRadius, 0f, mCameraCenterLocation[2]),
        CAMERA_FOV,
        1f,
        0f,
        0f
    )
    val cameraData: CameraData get() = mCameraData

    @Volatile
    protected var mViewportRatio = 1f

    @Volatile
    protected var mFigureVolumeCoef = 1f

    protected var mFigure: GLDrawing? = null
    protected val mFigureMutex = Mutex(false)

    @Volatile
    private var mBackgroundColor: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)
//    @Volatile
//    private var mDefaultModelColor: FloatArray = floatArrayOf(1f, 1f, 1f, 1f)

    protected var mDrawingSettings: MutableDrawingSettings = MutableDrawingSettings(
        GLContext.DrawingMode.FILLED,
        floatArrayOf(1f, 1f, 1f, 1f)
    )
    val drawingSettings: DrawingSettings get() = mDrawingSettings

    protected var mDeviceWidth: Int = 0
    protected var mDeviceHeight: Int = 0

    protected open val mInitializer: RendererStepInitializer = RendererStepInitializer()
    protected val mInitializerMutex = Mutex(false)

    suspend fun setCameraData(cameraData: CameraData) {//= mCameraMutex.withLock {
        Log.d(TAG, "setCameraData(): entering.. cameraData.pos = ${cameraData.position.joinToString()}")

        mInitializerMutex.withLock {
            mInitializer.postponeCamera(cameraData)

            if (mInitializer.currentStep != RendererStepInitializer.StandardStep.CAMERA)
                return

            onCameraStepInitializing()
        }
    }

    fun setDrawingSettings(drawingSettings: DrawingSettings) {
        mDrawingSettings.setData(drawingSettings)

        setFigureColor(mDrawingSettings.modelColor)
        mFigure?.setDrawingMode(mDrawingSettings.drawingMode)
    }

    fun setFigureDrawingMode(drawingMode: GLContext.DrawingMode) {
        mDrawingSettings.setDrawingMode(drawingMode)

        mFigure?.setDrawingMode(mDrawingSettings.drawingMode)

//        mFigure?.setDrawingMode(drawingMode)
    }

    fun setModelColor(
        @FloatRange(0.0, 1.0) r: Float,
        @FloatRange(0.0, 1.0) g: Float,
        @FloatRange(0.0, 1.0) b: Float,
        @FloatRange(0.0, 1.0) a: Float
    ) {
        mDrawingSettings.setModelColor(r, g, b, a)

        setFigureColor(mDrawingSettings.modelColor)


//        mDefaultModelColor = floatArrayOf(r, g, b, a)
//
//        mFigure?.apply {
//            setColor(mDefaultModelColor)
//        }
    }

    private fun setFigureColor(color: FloatArray) {
        mFigure?.apply { setColor(color) }
    }

    fun setBackgroundColor(
        @FloatRange(0.0, 1.0) r: Float,
        @FloatRange(0.0, 1.0) g: Float,
        @FloatRange(0.0, 1.0) b: Float,
        @FloatRange(0.0, 1.0) a: Float
    ) {
        mBackgroundColor = floatArrayOf(r, g, b, a)
    }

    protected open fun onCameraStepInitializing() {
        Log.d(TAG, "onCameraStepInitializing(): entering..")

        mCameraData.setData(mInitializer.camera!!)
        setPerspective()

        mInitializer.nextStep()
    }

    protected fun onFigureStepInitializing() {
        Log.d(TAG, "onFigureStepInitializing(): entering..")

        setFigureData(mInitializer.figure!!)

        mInitializer.nextStep()

        if (mInitializer.camera != null) onCameraStepInitializing()
    }

    suspend fun setFigure(figure: GLDrawing) {
        Log.d(TAG, "setFigure(): entering..")

        mInitializerMutex.withLock {
            if (mInitializer.currentStep != RendererStepInitializer.StandardStep.FIGURE)
                mInitializer.reset()

            mInitializer.postponeFigure(figure)

            mFigureMutex.withLock { onFigureStepInitializing() }
        }
    }

    protected fun setFigureData(figure: GLDrawing) {
        Log.d(TAG, "setFigureData(): entering..")

        prepareForFigure(figure)

        mCameraData.setMadeWayHorizontal(0f)
        mCameraData.setMadeWayVertical(0f)
        mCameraData.setScaleFactor(1f)

        setDefaultCameraLocation()
        setPerspective()
    }

    /**
     * Note: this method should be called DURING mFigureMutex LOCKING!
     */
    protected fun prepareForFigure(figure: GLDrawing) {
        Log.d(TAG, "prepareForFigure(): entering..")

        mFigure = figure.apply {
            setColor(mDrawingSettings.modelColor)
        }

        mViewCenterLocation = GL2Util.getVertexCenterPoint(mFigure!!.vertexArray)

        val sphereRadiusFromDistance =
            GL2Util.getMaxDistanceFromDot(mFigure!!.vertexArray, mViewCenterLocation)
        val minSphereRadius = DEFAULT_CAMERA_NEAR + CAMERA_NEAR_DRAWING_GAP
        val sphereRadius =
            if (sphereRadiusFromDistance > minSphereRadius) sphereRadiusFromDistance
            else minSphereRadius

        mSphereRadius = sphereRadius * DEFAULT_SPHERE_RADIUS_COEF
        mCameraRadius = mSphereRadius

        mCameraCenterLocation = mViewCenterLocation
        mFigureVolumeCoef = getFigureVolumeCoefBySphereRadiusAndMaxDistance(
            mSphereRadius, sphereRadiusFromDistance)
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
        val preparedDX = dx * -1 * mFigureVolumeCoef * (1 / mCameraData.scaleFactor)
        val preparedDY = dy * 1 * mFigureVolumeCoef * (1 / mCameraData.scaleFactor)

        var newX = mCameraData.position[0]
        var newY = mCameraData.position[1]
        var newZ = mCameraData.position[2]

        if (abs(preparedDX) >= abs(preparedDY)) {
            val cameraWayLength = getHorizontalCameraWayLength()
            val cameraMadeWay = (preparedDX + mCameraData.madeWayHorizontal) % cameraWayLength
            val cameraMadeWayNormalized =
                if (cameraMadeWay < 0) cameraMadeWay + cameraWayLength
                else cameraMadeWay

            val madeWayAngle = cameraMadeWayNormalized / mCameraRadius

            newX = mCameraCenterLocation[0] + mCameraRadius * cos(madeWayAngle)
            newY = mCameraCenterLocation[1] + mCameraRadius * sin(madeWayAngle)

            mCameraData.setMadeWayHorizontal(cameraMadeWayNormalized)

        } else {
            val cameraWayLength = getVerticalCameraWayLength()
            val cameraMadeWayNormalized = preparedDY + mCameraData.madeWayVertical

            if (abs(cameraMadeWayNormalized) >= cameraWayLength) return mCameraData.position

            val madeWayAngleVertical = cameraMadeWayNormalized / mSphereRadius

            newZ = CENTER_POSITION[2] + mSphereRadius * sin(madeWayAngleVertical)
            val newCameraRadius = sqrt(mSphereRadius * mSphereRadius - newZ * newZ)

            mCameraData.apply {
                setMadeWayHorizontal(madeWayHorizontal * (newCameraRadius / mCameraRadius))
            }
            mCameraRadius = newCameraRadius

            val madeWayAngleHorizontal = mCameraData.madeWayHorizontal / mCameraRadius

            newX = mCameraCenterLocation[0] + mCameraRadius * cos(madeWayAngleHorizontal)
            newY = mCameraCenterLocation[1] + mCameraRadius * sin(madeWayAngleHorizontal)

            mCameraData.setMadeWayVertical(cameraMadeWayNormalized)
        }

        return floatArrayOf(newX, newY, newZ)
    }

    open fun handleRotation(dx: Float, dy: Float) {//= mCameraMutex.withLock {
        mCameraData.setPosition(getTranslatedCameraLocation(dx, dy))
    }

    open fun handleScale(gottenScaleFactor: Float) {//= mCameraMutex.withLock {
        val newScaleFactor = mCameraData.scaleFactor * gottenScaleFactor

        if (newScaleFactor !in MIN_SCALE_FACTOR..MAX_SCALE_FACTOR) return

        Log.d(TAG, "handleScale(): newScaleFactor = $newScaleFactor; scaleFactor = $gottenScaleFactor;")

        mCameraData.setScaleFactor(newScaleFactor)

        setPerspective()
    }

    protected fun setPerspective() {
        mCameraData.apply { setFOV(CAMERA_FOV / scaleFactor) }

        Log.d(TAG, "setPerspective(): mCameraData.fov = ${mCameraData.fov}; mCameraData.scaleFactor = ${mCameraData.scaleFactor}")

        Matrix.perspectiveM(
            mProjectionMatrix, 0,
            mCameraData.fov,
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

        mCameraData.setPosition(floatArrayOf(
            mViewCenterLocation[0] + mCameraRadius, mViewCenterLocation[1], mViewCenterLocation[2]))
        //mCameraLocation = getTranslatedCameraLocation(0f, initCameraVerticalMadeWay.toFloat())

        Log.d(TAG, "setDefaultCameraLocation(): mCameraLocation = (${mCameraData.position.joinToString()});")
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(mBackgroundColor[0], mBackgroundColor[1], mBackgroundColor[2], mBackgroundColor[3])

        mFigure?.init()

        runBlocking { setDefaultCameraLocation() }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mDeviceWidth = width
        mDeviceHeight = height

        GLES20.glViewport(0, 0, width, height)

        mViewportRatio = width.toFloat() / height.toFloat()

        runBlocking { setPerspective() }
    }

    override fun onDrawFrame(gl: GL10?): Unit = runBlocking {
        mFigureMutex.withLock {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

            Matrix.setLookAtM(
                mViewMatrix, 0,
                mCameraData.position[0],
                mCameraData.position[1],
                mCameraData.position[2],
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
}