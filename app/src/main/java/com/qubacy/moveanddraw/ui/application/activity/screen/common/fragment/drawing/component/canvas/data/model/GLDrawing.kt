package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model

import android.opengl.GLES20
import com.qubacy.moveanddraw._common.util.struct.array.toNativeBuffer
import com.qubacy.moveanddraw.domain._common.model.drawing._common.DrawingContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas._common.GLContext
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.util.GL2Util
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.FloatBuffer
import java.nio.IntBuffer

open class GLDrawing (
    vertexArray: FloatArray,
    vertexDrawingOrder: IntArray? = null,
    drawingMode: GLContext.DrawingMode = GLContext.DrawingMode.FILLED,
    color: FloatArray = floatArrayOf(1f, 1f, 1f, 1f)
) {
    companion object {

    }

    protected open val mVertexShaderCode =
        "uniform mat4 uVPMatrix;" +
        "attribute vec4 vPosition;" +
        "attribute vec4 vNormal;" +
        "void main() {" +
        "  gl_PointSize = 5.0;" +
        "  gl_Position = uVPMatrix * vPosition;" +
        "}"
    protected open val mFragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
//        "uniform vec4 cameraPos;" +
        "void main() {" +
//        "  lightVec = cameraPos - gl_Position;" +
//        "  cosine = dot product(object normal, normalize(lightVec));" +
        "  gl_FragColor = vColor;" +
        "}"
    protected var mProgram: Int = 0

    protected var mVPMatrixHandle: Int = 0

    private var mIsInitialized = false
    val isInitialized get () = mIsInitialized

    @Volatile
    protected var mVertexBuffer: FloatBuffer = vertexArray.toNativeBuffer()
    @Volatile
    protected var mVertexDrawingOrderBuffer: IntBuffer? = vertexDrawingOrder?.toNativeBuffer()

    @Volatile
    private var mVertexArray: FloatArray = vertexArray
    @Volatile
    private var mVertexDrawingOrder: IntArray? = vertexDrawingOrder

    val vertexArray get() = mVertexArray
    val vertexDrawingOrder get() = mVertexDrawingOrder

    protected val mMutex: Mutex = Mutex(false)
    protected val mVertexCount get() = vertexArray.size / DrawingContext.COORDS_PER_VERTEX

    @Volatile
    private var mColor: FloatArray = color
    val color get() = mColor

    @Volatile
    private var mDrawingMode: GLContext.DrawingMode = drawingMode

    fun init() {
        val vertexShader = GL2Util.loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderCode)
        val fragmentShader = GL2Util.loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderCode)

        mProgram = GLES20.glCreateProgram().apply {
            GLES20.glAttachShader(this, vertexShader)
            GLES20.glAttachShader(this, fragmentShader)

            GLES20.glLinkProgram(this)
        }

        mIsInitialized = true
    }

    fun setDrawingMode(drawingMode: GLContext.DrawingMode) {
        mDrawingMode = drawingMode
    }

    fun setColor(rgba: FloatArray) {
        mColor = rgba
    }

    suspend fun setVertices(
        vertexArray: FloatArray,
        vertexDrawingOrder: IntArray? = null
    ) = mMutex.withLock {
        mVertexArray = vertexArray
        mVertexDrawingOrder = vertexDrawingOrder

        mVertexBuffer = mVertexArray.toNativeBuffer()
        mVertexDrawingOrderBuffer = mVertexDrawingOrder?.toNativeBuffer()
    }

    fun draw(mvpMatrix: FloatArray) = runBlocking {
        mMutex.withLock {
            GLES20.glUseProgram(mProgram)

            mVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uVPMatrix")

            GLES20.glUniformMatrix4fv(mVPMatrixHandle, 1, false, mvpMatrix, 0)

            GLES20.glGetAttribLocation(mProgram, "vPosition").also {
                GLES20.glEnableVertexAttribArray(it)
                GLES20.glVertexAttribPointer(
                    it,
                    DrawingContext.COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT,
                    false,
                    DrawingContext.COORDS_PER_VERTEX * Float.SIZE_BYTES,
                    mVertexBuffer
                )
                GLES20.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->
                    GLES20.glUniform4fv(colorHandle, 1, mColor, 0)

                    if (mVertexCount <= 0) {  }
                    else if (mVertexCount == 1) drawElementsWithGLMode(GLES20.GL_POINTS)
                    else if (mVertexCount == 2) drawElementsWithGLMode(GLES20.GL_LINES)
                    else drawElementsWithDrawingModeAndColor(mDrawingMode, colorHandle)
                }

                GLES20.glDisableVertexAttribArray(it)
            }
        }
    }

    private fun generateOutlineColor(): FloatArray {
        return mColor.mapIndexed { i, it ->
            if (i != mColor.size - 1) (it + 0.3f) % 1 else it
        }.toFloatArray()
    }

    private fun drawElementsWithDrawingModeAndColor(
        drawingMode: GLContext.DrawingMode,
        colorHandle: Int
    ) {
        when (drawingMode) {
            GLContext.DrawingMode.SKETCH -> drawElementsWithGLMode(GLES20.GL_LINE_LOOP)
            GLContext.DrawingMode.FILLED -> drawElementsWithGLMode(GLES20.GL_TRIANGLES)
            GLContext.DrawingMode.OUTLINED -> {
                drawElementsWithGLMode(GLES20.GL_TRIANGLES)

                val outlineColor = generateOutlineColor()

                GLES20.glUniform4fv(colorHandle, 1, outlineColor, 0)

                drawElementsWithGLMode(GLES20.GL_LINE_LOOP)
            }
        }
    }

    private fun drawElementsWithGLMode(glMode: Int) {
        if (mVertexDrawingOrderBuffer != null)
            GLES20.glDrawElements(
                glMode, mVertexDrawingOrder!!.size,
                GLES20.GL_UNSIGNED_INT, mVertexDrawingOrderBuffer)
        else
            GLES20.glDrawArrays(glMode, 0, mVertexCount)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GLDrawing

        if (!mVertexArray.contentEquals(other.mVertexArray)) return false
        if (!mVertexDrawingOrder.contentEquals(other.mVertexDrawingOrder)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mVertexArray.contentHashCode()

        result = 31 * result + mVertexDrawingOrder.contentHashCode()

        return result
    }
}