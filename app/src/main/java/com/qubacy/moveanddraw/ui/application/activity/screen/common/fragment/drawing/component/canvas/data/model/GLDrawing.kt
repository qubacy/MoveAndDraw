package com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.component.canvas.data.model

import android.opengl.GLES20
import com.qubacy.moveanddraw.ui.application.activity.screen.common.fragment.drawing.util.GL2Util
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

open class GLDrawing (
    val vertexArray: FloatArray,
    private val mVertexDrawingOrder: ShortArray? = null,
    val color: FloatArray = floatArrayOf(1f, 1f, 1f, 1f)
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

    protected val mVertexBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(vertexArray.size * Float.SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertexArray)
                position(0)
            }
        }
    private val mVertexDrawingOrderBuffer: ShortBuffer? = if (mVertexDrawingOrder != null)
        ByteBuffer.allocateDirect(mVertexDrawingOrder.size * Short.SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(mVertexDrawingOrder)
                position(0)
            }
        }
    else null
    private val mVertexCount = vertexArray.size / COORDS_PER_VERTEX

    private var mIsInitialized = false
    val isInitialized get () = mIsInitialized

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

    fun draw(mvpMatrix: FloatArray) {
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
                GLES20.glUniform4fv(colorHandle, 1, color, 0)
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