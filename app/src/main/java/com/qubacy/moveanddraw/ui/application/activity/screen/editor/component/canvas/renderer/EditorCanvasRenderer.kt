package com.qubacy.moveanddraw.ui.application.activity.screen.editor.component.canvas.renderer

import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model.GLDrawing
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.renderer.CanvasRenderer
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class EditorCanvasRenderer(

) : CanvasRenderer() {
    companion object {
        val DEVICE_DRAWING_COLOR = floatArrayOf(0f, 1f, 0f, 1f)
        const val DEVICE_DRAWING_SIZE = 0.2f
    }

    @Volatile
    private lateinit var mDeviceDrawing: GLDrawing

    private val mDeviceDrawingMutex: Mutex = Mutex(false)

    private fun generateDeviceGLDrawing(x: Float, y: Float, z: Float): GLDrawing {
        return GLDrawing(
            floatArrayOf(
                -DEVICE_DRAWING_SIZE + x, -DEVICE_DRAWING_SIZE + y, z,
                -DEVICE_DRAWING_SIZE + x, DEVICE_DRAWING_SIZE + y, z,
                DEVICE_DRAWING_SIZE + x, DEVICE_DRAWING_SIZE + y, z,
                DEVICE_DRAWING_SIZE + x, -DEVICE_DRAWING_SIZE + y, z,
                x, y, DEVICE_DRAWING_SIZE + z,
                x, y, -DEVICE_DRAWING_SIZE + z
            ),
            shortArrayOf(
                0, 1, 4,
                1, 2, 4,
                2, 3, 4,
                0, 3, 4,
                0, 1, 5,
                1, 2, 5,
                2, 3, 5,
                0, 3, 5
            ),
            DEVICE_DRAWING_COLOR
        )
    }

    suspend fun setDeviceDrawingPosition(x: Float, y: Float, z: Float) {
        mDeviceDrawingMutex.lock()

        mDeviceDrawing = generateDeviceGLDrawing(x, y, z)
        mDeviceDrawing.init()

        mDeviceDrawingMutex.unlock()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)

        runBlocking {
            setDeviceDrawingPosition(0f, 0f, 0f)
        }
    }

    override fun onDrawFrame(gl: GL10?) = runBlocking {
        super.onDrawFrame(gl)

        mDeviceDrawingMutex.lock()

        mDeviceDrawing.draw(mVPMatrix)

        mDeviceDrawingMutex.unlock()
    }
}