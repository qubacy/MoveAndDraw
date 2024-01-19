package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model

import android.opengl.GLES20
import com.qubacy.moveanddraw._common.util.struct.array.toNativeBuffer
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.util.GL2Util
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

open class GLDrawing (
    vertexArray: FloatArray,
    vertexDrawingOrder: ShortArray? = null,
    color: FloatArray = floatArrayOf(1f, 1f, 1f, 1f)
) {
    companion object {
        const val COORDS_PER_VERTEX = 3
    }

    protected open val mVertexShaderCode =
        "uniform mat4 uVPMatrix;" +
        "attribute vec4 vPosition;" +
        "void main() {" +
        "  gl_Position = uVPMatrix * vPosition;" +
        "}"
    protected open val mFragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "void main() {" +
        "  gl_FragColor = vColor;" +
        "}"
    protected var mProgram: Int = 0

    protected var mVPMatrixHandle: Int = 0

    private var mIsInitialized = false
    val isInitialized get () = mIsInitialized

    @Volatile
    protected var mVertexBuffer: FloatBuffer = vertexArray.toNativeBuffer()
    @Volatile
    protected var mVertexDrawingOrderBuffer: ShortBuffer? = vertexDrawingOrder?.toNativeBuffer()

    @Volatile
    private var mVertexArray: FloatArray = vertexArray
    @Volatile
    private var mVertexDrawingOrder: ShortArray? = vertexDrawingOrder

    val vertexArray get() = mVertexArray
    val vertexDrawingOrder get() = mVertexDrawingOrder

    protected val mMutex: Mutex = Mutex(false)

    @Volatile
    protected var mVertexCount = vertexArray.size / COORDS_PER_VERTEX

    @Volatile
    private var mColor: FloatArray = color
    val color get() = mColor

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

    fun setColor(rgba: FloatArray) {
        mColor = rgba
    }

    suspend fun setVertices(
        vertexArray: FloatArray,
        vertexDrawingOrder: ShortArray? = null
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
                    COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT,
                    false,
                    COORDS_PER_VERTEX * Float.SIZE_BYTES,
                    mVertexBuffer
                )
                GLES20.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->
                    GLES20.glUniform4fv(colorHandle, 1, mColor, 0)
                }

                if (mVertexDrawingOrderBuffer != null)
                    GLES20.glDrawElements(
                        GLES20.GL_TRIANGLES, mVertexDrawingOrder!!.size,
                        GLES20.GL_UNSIGNED_SHORT, mVertexDrawingOrderBuffer)
                else
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount)

                GLES20.glDisableVertexAttribArray(it)
            }
        }
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