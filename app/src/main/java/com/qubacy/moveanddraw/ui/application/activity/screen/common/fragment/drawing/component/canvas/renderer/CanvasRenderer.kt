package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import androidx.annotation.FloatRange
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.GLContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.camera._common.CameraContext
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

        const val DEFAULT_SPHERE_RADIUS = 1f
        const val DEFAULT_SPHERE_RADIUS_COEF = 3f

        const val VERTICAL_CAMERA_WAY_LIMIT_COEF = 0.8
    }

    protected val mProjectionMatrix = FloatArray(16)
    protected val mViewMatrix = FloatArray(16)
    protected val mVPMatrix = FloatArray(16)

    protected var mSphereRadius: Float = DEFAULT_SPHERE_RADIUS
    protected var mCameraRadius = mSphereRadius

    @Volatile
    protected var mViewCenterLocation = floatArrayOf(0f, 0f, 0f)

    protected var mCameraData: MutableCameraData = MutableCameraData(
        floatArrayOf(mCameraRadius, 0f, mViewCenterLocation[2]),
        CameraContext.DEFAULT_CAMERA_FOV,
        1f,
        0f,
        0f,
        CameraContext.DEFAULT_CAMERA_NEAR
    )

    @Volatile
    protected var mViewportRatio = 1f

    @Volatile
    protected var mFigureVolumeCoef = 1f

    protected var mFigure: GLDrawing? = null
    protected val mFigureMutex = Mutex(false)

    // TODO: mb mBackgroundColor could be moved to mDrawingSettings obj. as a new field;
    @Volatile
    private var mBackgroundColor: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

    protected var mDrawingSettings: MutableDrawingSettings = MutableDrawingSettings(
        GLContext.DrawingMode.FILLED,
        floatArrayOf(1f, 1f, 1f, 1f)
    )
    val drawingSettings: DrawingSettings get() = mDrawingSettings

    protected var mDeviceWidth: Int = 0
    protected var mDeviceHeight: Int = 0

    protected open val mInitializer: RendererStepInitializer = RendererStepInitializer()
    protected val mInitializerMutex = Mutex(false)

    fun getCameraData(): CameraData? {
        if (mFigure == null) return null

        return mCameraData.copy()
    }

    fun resetInitializer() {
        mInitializer.reset()
    }

    suspend fun setCameraData(cameraData: CameraData) {
        mInitializerMutex.withLock {
            Log.d(
                TAG,
                "setCameraData(): entering.. cameraData.pos = ${cameraData.position.joinToString()}"
            )
            Log.d(TAG, "setCameraData(): mInitializer.currentStep = ${mInitializer.currentStep};")


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
    }

    fun setModelColor(
        @FloatRange(0.0, 1.0) r: Float,
        @FloatRange(0.0, 1.0) g: Float,
        @FloatRange(0.0, 1.0) b: Float,
        @FloatRange(0.0, 1.0) a: Float
    ) {
        mDrawingSettings.setModelColor(r, g, b, a)

        setFigureColor(mDrawingSettings.modelColor)
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

        changeCameraData(mInitializer.camera!!)

        mInitializer.nextStep()
    }

    private fun changeCameraData(cameraData: CameraData) {
        mCameraData.setData(cameraData)
        setPerspective()
    }

    /**
     * Executing under mInitializerMutex;
     */
    protected fun onFigureStepInitializing() {
        Log.d(TAG, "onFigureStepInitializing(): entering..")

        setFigureData(mInitializer.figure!!)

        mInitializer.nextStep()

        if (mInitializer.camera != null) onCameraStepInitializing()
    }

    suspend fun setFigure(figure: GLDrawing) {
        mInitializerMutex.withLock {
            Log.d(TAG,
                "setFigure(): figure.vertexArray.size = ${figure.vertexArray.size};" +
                " mInitializer.currentStep = ${mInitializer.currentStep};"
            )
            Log.d(TAG, "setFigure(): mInitializer.currentStep = ${figure.vertexArray.size};")

            if (mInitializer.currentStep != RendererStepInitializer.StandardStep.FIGURE)
                mInitializer.reset()

            mInitializer.postponeFigure(figure)

            mFigureMutex.withLock { onFigureStepInitializing() }
        }
    }

    protected fun setFigureData(figure: GLDrawing) {
        Log.d(TAG, "setFigureData(): entering..")

        prepareForFigure(figure)

        mCameraData.setMadeWayHorizontal(CameraContext.DEFAULT_MADE_WAY)
        mCameraData.setMadeWayVertical(CameraContext.DEFAULT_MADE_WAY)
        mCameraData.setScaleFactor(CameraContext.DEFAULT_SCALE)

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
            setDrawingMode(mDrawingSettings.drawingMode)
        }

        mViewCenterLocation = GL2Util.getVertexCenterPoint(mFigure!!.vertexArray)

        val sphereRadiusFromDistance =
            GL2Util.getMaxDistanceFromDot(mFigure!!.vertexArray, mViewCenterLocation)
        val minSphereRadius = CameraContext.DEFAULT_CAMERA_NEAR + CameraContext.CAMERA_NEAR_DRAWING_GAP
        val sphereRadius =
            if (sphereRadiusFromDistance > minSphereRadius) sphereRadiusFromDistance
            else minSphereRadius

        mSphereRadius = sphereRadius * DEFAULT_SPHERE_RADIUS_COEF
        mCameraRadius = mSphereRadius

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

    fun getVerticalCameraWayLength(): Float {
        return (VERTICAL_CAMERA_WAY_LIMIT_COEF * PI * mSphereRadius / 2).toFloat()
    }

    fun getHorizontalCameraWayLength(): Float {
        return (2 * PI * mCameraRadius).toFloat()
    }

    // todo: think of mCameraRadius. shouldn't it be preserved as well?
    fun getTranslatedCameraLocation(dx: Float, dy: Float): FloatArray {
        val preparedDX = dx * -1 * mFigureVolumeCoef * (1 / mCameraData.scaleFactor)
        val preparedDY = dy * 1 * mFigureVolumeCoef * (1 / mCameraData.scaleFactor)

        var newX = mCameraData.position[0]
        var newY = mCameraData.position[1]
        var newZ = mCameraData.position[2]

        // TODO: it'd be better to separate these two cases and compose new methods;
        if (abs(preparedDX) >= abs(preparedDY)) {
            val cameraWayLength = getHorizontalCameraWayLength()
            val cameraMadeWay = (preparedDX + mCameraData.madeWayHorizontal) % cameraWayLength
            val cameraMadeWayNormalized =
                if (cameraMadeWay < 0) cameraMadeWay + cameraWayLength
                else cameraMadeWay

            val madeWayAngle = cameraMadeWayNormalized / mCameraRadius

            newX = mViewCenterLocation[0] + mCameraRadius * cos(madeWayAngle)
            newY = mViewCenterLocation[1] + mCameraRadius * sin(madeWayAngle)

            mCameraData.setMadeWayHorizontal(cameraMadeWayNormalized)

        } else {
            val cameraWayLength = getVerticalCameraWayLength()
            val cameraMadeWay = preparedDY + mCameraData.madeWayVertical
            val cameraMadeWayNormalized =
                if (cameraMadeWay > cameraWayLength) cameraWayLength
                else if (cameraMadeWay < -cameraWayLength) -cameraWayLength
                else cameraMadeWay

            val madeWayAngleVertical = cameraMadeWayNormalized / mSphereRadius

            newZ = mViewCenterLocation[2] + mSphereRadius * sin(madeWayAngleVertical)

            val cameraHeight = newZ - mViewCenterLocation[2]
            val newCameraRadius = sqrt(mSphereRadius * mSphereRadius - cameraHeight * cameraHeight)

            mCameraData.apply {
                setMadeWayHorizontal(madeWayHorizontal * (newCameraRadius / mCameraRadius))
            }
            mCameraRadius = newCameraRadius

            val madeWayAngleHorizontal = mCameraData.madeWayHorizontal / mCameraRadius

            newX = mViewCenterLocation[0] + mCameraRadius * cos(madeWayAngleHorizontal)
            newY = mViewCenterLocation[1] + mCameraRadius * sin(madeWayAngleHorizontal)

            mCameraData.setMadeWayVertical(cameraMadeWayNormalized)
        }

        return floatArrayOf(newX, newY, newZ)
    }

    open fun handleRotation(dx: Float, dy: Float) {
        mCameraData.setPosition(getTranslatedCameraLocation(dx, dy))
    }

    open fun handleScale(gottenScaleFactor: Float) {
        val newScaleFactor = mCameraData.scaleFactor * gottenScaleFactor

        Log.d(TAG, "handleScale(): newScaleFactor = $newScaleFactor;")

        if (!CameraContext.checkScaleFactorValidity(newScaleFactor)) return

        mCameraData.setScaleFactor(newScaleFactor)

        setPerspective()
    }

    protected fun setPerspective() {
        Matrix.perspectiveM(
            mProjectionMatrix, 0,
            mCameraData.fov,
            mViewportRatio,
            mCameraData.cameraNear,
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
        Log.d(TAG, "onSurfaceCreated(): entering..")

        GLES20.glClearColor(mBackgroundColor[0], mBackgroundColor[1], mBackgroundColor[2], mBackgroundColor[3])

        runBlocking {
            if (mFigure?.isInitialized == false) mFigure?.init()

            if (!mInitializer.isStepPassed(RendererStepInitializer.StandardStep.CAMERA))
                setDefaultCameraLocation()
        }
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