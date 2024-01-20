package com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.renderer

import android.opengl.Matrix
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model.GLDrawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer.CanvasRenderer
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class EditorCanvasRenderer(

) : CanvasRenderer() {
    companion object {
        val HELPING_PLANE_DRAWING_COLOR = floatArrayOf(0f, 1f, 0f, 0.3f)

        const val DEFAULT_HELPING_PLANE_DISTANCE = 2f
        const val MIN_HELPING_PLANE_DISTANCE = 1f
    }

    private val mHelpingPlaneDrawing: GLDrawing = generateHelpingPlaneGLDrawing()

    private val mHelpingPlaneDrawingMutex: Mutex = Mutex(false)

    private var mIsHelpingPlaneVisible: Boolean = true // todo: change to false;
    private var mHelpingPlaneDistance: Float = DEFAULT_HELPING_PLANE_DISTANCE

    val helpingPlaneDistance get() = mHelpingPlaneDistance

    fun setHelpingPlaneVisible(isVisible: Boolean) {
        mIsHelpingPlaneVisible = isVisible
    }

    fun setHelpingPlaneDistance(distance: Float) {
        if (distance !in MIN_HELPING_PLANE_DISTANCE..(mSphereRadius * 2))
            return

        mHelpingPlaneDistance = distance
    }

    private fun generateHelpingPlaneGLDrawing(): GLDrawing {
        return GLDrawing(
            floatArrayOf(0f, 0f, 0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 0f, 0f),
            shortArrayOf(0, 1, 2, 0, 2, 3),
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
        val normalizedHelpingPlaneDistance = 0.95f + (mHelpingPlaneDistance / (mSphereRadius * 2)) / 20

        val helpingPlaneVertices = arrayOf(
            1f, 1f, normalizedHelpingPlaneDistance,//0.95f, //0.999f,
            -1f, 1f, normalizedHelpingPlaneDistance,//0.95f, //0.999f,
            -1f, -1f, normalizedHelpingPlaneDistance,//0.95f, //0.999f,
            1f, -1f, normalizedHelpingPlaneDistance,//0.95f //0.999f
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