package com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.renderer

import android.opengl.Matrix
import android.util.Log
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.GLContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model.GLDrawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer.CanvasRenderer
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class EditorCanvasRenderer(

) : CanvasRenderer() {
    enum class EditorRendererMode {
        VIEWING, CREATING_FACE;
    }

    companion object {
        val HELPING_PLANE_DRAWING_COLOR = floatArrayOf(0f, 1f, 0f, 0.4f)

        const val MIN_HELPING_PLANE_DISTANCE = DEFAULT_CAMERA_NEAR
        const val HELPING_PLANE_MODEL_GAP = 0.001f
    }

    private val mHelpingPlaneDrawing: GLDrawing = generateHelpingPlaneGLDrawing()

    private val mHelpingPlaneDrawingMutex: Mutex = Mutex(false)

    private var mEditorRendererMode: EditorRendererMode = EditorRendererMode.VIEWING
    private var mIsHelpingPlaneVisible: Boolean = false

    fun setMode(mode: EditorRendererMode) {
        mEditorRendererMode = mode

        setHelpingPlaneVisibilityByMode(mode)
        changeCameraNearByMode(mode)
    }

    private fun changeCameraNearByMode(mode: EditorRendererMode) {
        val cameraNear = when (mode) {
            EditorRendererMode.VIEWING -> DEFAULT_CAMERA_NEAR
            EditorRendererMode.CREATING_FACE -> MIN_HELPING_PLANE_DISTANCE
        }

        changeCameraNear(cameraNear) // todo: isn't working..
    }

    private fun setHelpingPlaneVisibilityByMode(mode: EditorRendererMode) {
        mIsHelpingPlaneVisible = when (mode) {
            EditorRendererMode.VIEWING -> false
            EditorRendererMode.CREATING_FACE -> true
        }
    }

//    fun setHelpingPlaneDistance(distance: Float) {
//        if (distance !in MIN_HELPING_PLANE_DISTANCE..(mSphereRadius * 2))
//            return
//
//        mHelpingPlaneDistance = distance
//    }

    override fun handleScale(scaleFactor: Float) {
        when (mEditorRendererMode) {
            EditorRendererMode.CREATING_FACE -> handleHelpingPlaneDistanceChange(scaleFactor)
            else -> super.handleScale(scaleFactor)
        }
    }

    override fun handleRotation(dx: Float, dy: Float) {
        when (mEditorRendererMode) {
            EditorRendererMode.CREATING_FACE -> {  } // nothing?
            else -> super.handleRotation(dx, dy)
        }
    }

    private fun handleHelpingPlaneDistanceChange(distanceFactor: Float) {
        val newNear = mCameraNear * distanceFactor
        val filteredNewNear =
            if (newNear <= MIN_HELPING_PLANE_DISTANCE) MIN_HELPING_PLANE_DISTANCE
            else if (newNear >= mSphereRadius * 2) mSphereRadius * 2 - HELPING_PLANE_MODEL_GAP
            else newNear

        changeCameraNear(filteredNewNear)
    }

    private fun changeCameraNear(near: Float) {
        Log.d(TAG, "changeCameraNear(): near = $near")

        mCameraNear = near

        setPerspective()
    }

    private fun generateHelpingPlaneGLDrawing(): GLDrawing {
        return GLDrawing(
            floatArrayOf(0f, 0f, 0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 0f, 0f),
            shortArrayOf(0, 1, 2, 0, 2, 3),
            GLContext.DrawingMode.FILLED,
            HELPING_PLANE_DRAWING_COLOR
        )
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
    }

    override fun onDrawFrame(gl: GL10?): Unit = runBlocking {
        super.onDrawFrame(gl)

        drawHelpingPlane()
    }

    private suspend fun drawHelpingPlane() = mHelpingPlaneDrawingMutex.withLock {
        if (!mIsHelpingPlaneVisible) return@withLock
        if (!mHelpingPlaneDrawing.isInitialized) mHelpingPlaneDrawing.init()

        val helpingPlaneVertices = getHelpingPlaneVertices()

        mHelpingPlaneDrawing.setVertices(helpingPlaneVertices, mHelpingPlaneDrawing.vertexDrawingOrder)
        mHelpingPlaneDrawing.draw(mVPMatrix)
    }

    private fun getHelpingPlaneVertices(): FloatArray {
        val normalizedHelpingPlaneZ = -1f + HELPING_PLANE_MODEL_GAP

        val helpingPlaneVertices = arrayOf(
            1f, 1f, normalizedHelpingPlaneZ,
            -1f, 1f, normalizedHelpingPlaneZ,
            -1f, -1f, normalizedHelpingPlaneZ,
            1f, -1f, normalizedHelpingPlaneZ
        )

        val projToWorldCoordsMatrix = FloatArray(16)
        Matrix.invertM(projToWorldCoordsMatrix, 0, mVPMatrix, 0)

        val projHelpingPlaneVertices = FloatArray(helpingPlaneVertices.size)

        for (i in helpingPlaneVertices.indices step GLDrawing.COORDS_PER_VERTEX) {
            val curVertex = floatArrayOf(
                helpingPlaneVertices[i], helpingPlaneVertices[i + 1], helpingPlaneVertices[i + 2], 1f
            )
            val projVertex = FloatArray(curVertex.size)

            Matrix.multiplyMV(
                projVertex, 0,
                projToWorldCoordsMatrix, 0,
                curVertex, 0
            )

            val normalizedProjVertex = projVertex.map { it / projVertex[3] }.toFloatArray()

            normalizedProjVertex.copyInto(
                projHelpingPlaneVertices, i, 0, GLDrawing.COORDS_PER_VERTEX)
        }

        return projHelpingPlaneVertices
    }
}