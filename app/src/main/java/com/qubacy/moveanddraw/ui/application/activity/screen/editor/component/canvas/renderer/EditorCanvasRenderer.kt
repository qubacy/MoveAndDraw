package com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.renderer

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
    }

    private val mHelpingPlaneDrawing: GLDrawing = generateHelpingPlaneGLDrawing()

    private val mHelpingPlaneDrawingMutex: Mutex = Mutex(false)

    private fun generateHelpingPlaneGLDrawing(): GLDrawing {
        return GLDrawing(
            floatArrayOf(0f, 0f, 0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 0f, 0f),
            shortArrayOf(0, 1, 2, 0, 2, 3),
            HELPING_PLANE_DRAWING_COLOR
        )
    }

    private suspend fun setHelpingPlaneDrawingPosition(
        x: Float, y: Float, z: Float
    ) = mHelpingPlaneDrawingMutex.withLock {
        // todo: implement..


    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
    }

    override fun onDrawFrame(gl: GL10?): Unit = runBlocking {
        super.onDrawFrame(gl)

        mHelpingPlaneDrawingMutex.withLock {
            if (!mHelpingPlaneDrawing.isInitialized) mHelpingPlaneDrawing.init()

            mHelpingPlaneDrawing.draw(mVPMatrix)
        }
    }
}